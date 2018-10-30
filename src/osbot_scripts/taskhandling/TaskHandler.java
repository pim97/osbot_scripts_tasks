package osbot_scripts.taskhandling;

import java.util.HashMap;
import java.util.Map.Entry;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import osbot_scripts.bot.utils.BotCommands;
import osbot_scripts.database.DatabaseUtilities;
import osbot_scripts.events.LoginEvent;
import osbot_scripts.events.MandatoryEventsExecution;
import osbot_scripts.framework.AccountStage;
import osbot_scripts.qp7.progress.QuestStep;
import osbot_scripts.task.Task;

public class TaskHandler {

	public TaskHandler(MethodProvider provider, QuestStep quest, LoginEvent event, Script script) {
		this.provider = provider;
		this.quest = quest;
		this.event = event;
		this.script = script;
		this.setEvents(new MandatoryEventsExecution(provider));
	}

	private HashMap<Integer, Task> tasks = new HashMap<Integer, Task>();

	private Task currentTask;

	private QuestStep quest;

	private MethodProvider provider;

	private LoginEvent event;

	private Script script;

	private MandatoryEventsExecution events;

	private long lastTask = System.currentTimeMillis();

	private static final Area TUT_ISLAND_AREA = new Area(
			new int[][] { { 3049, 3128 }, { 3032, 3090 }, { 3054, 3037 }, { 3146, 3048 }, { 3181, 3101 },
					{ 3163, 3129 }, { 3137, 3137 }, { 3131, 3145 }, { 3103, 3140 }, { 3087, 3156 }, { 3054, 3138 } });

	private static final Area TUT_ISLAND_AREA_CAVE = new Area(new int[][] { { 3067, 9521 }, { 3096, 9540 },
			{ 3128, 9540 }, { 3125, 9498 }, { 3085, 9482 }, { 3062, 9500 } });

	private static final Area LUMBRIDGE_AREA_START = new Area(
			new int[][] { { 3212, 3246 }, { 3273, 3246 }, { 3272, 3190 }, { 3212, 3190 } });

	public void taskLoop() throws InterruptedException {
		if (getProvider().getClient().isLoggedIn()) {

			// Checking if acc is on tutorial island
			if (TUT_ISLAND_AREA.contains(getProvider().myPlayer())
					|| TUT_ISLAND_AREA_CAVE.contains(getProvider().myPlayer())) {
				if (getEvent() != null && getEvent().getUsername() != null) {
					DatabaseUtilities.updateStageProgress(getProvider(), AccountStage.TUT_ISLAND.name(), 0,
							getEvent().getUsername());
					BotCommands.killProcess(getScript());
					getProvider().log("Account didn't belong here, sending back to tutorial island");
				}
			}

			// Checking if he account is not fixed mode (client)
			getEvents().fixedMode();
			getEvents().fixedMode2();
			getEvents().executeAllEvents();
		}

		// Checking is the account is not logged in
		if (!getProvider().getClient().isLoggedIn()) {
			DatabaseUtilities.updateAccountStatusInDatabase(getProvider(), "TIMEOUT", getEvent().getUsername());
			BotCommands.killProcess(getScript());
		}

		// Is the account too long logged in without doing anything? Set it to
		// stuck/timeout, it will run the client again with WebWalking enabled to
		// un-stuck the person
		long currentTask = System.currentTimeMillis();

		getProvider().log("Task time: " + (currentTask - lastTask));
		if (lastTask != 0 && currentTask - lastTask > 60000) {
			getProvider().log("Took too much time, proably stuck!");
			if (getEvent() != null && getEvent().getUsername() != null) {
				DatabaseUtilities.updateAccountStatusInDatabase(getProvider(),
						getEvent().getAccountStage().equalsIgnoreCase("WALKING-STUCK") ? "WALKING_STUCK" : "TIMEOUT",
						getEvent().getUsername());
			}
			System.exit(1);
		}

		// Has the task corresponding with the character been found or not?
		boolean foundTask = false;

		// Looping over all the tasks
		for (Entry<Integer, Task> entry : getTasks().entrySet()) {
			int taskAttempts = 0;

			int questStepRequired = entry.getKey();
			Task task = entry.getValue();

			// Finding new task out of the database (the number) when starting
			if (getCurrentTask() == null
					&& (questStepRequired == getQuest().getQuestStageStep() || (currentTask - lastTask) > 30000)
					&& task.requiredConfigQuestStep() == getQuest().getQuestProgress()) {
				setCurrentTask(task);
				getProvider().log("On next task new: " + task.scriptName() + " " + task.requiredConfigQuestStep() + " "
						+ getQuest().getQuestProgress() + " " + getQuest().getQuestStageStep() + " "
						+ questStepRequired);
				foundTask = true;
				// Set a task when null
			}

			// Has the script found a task already?
			if (!foundTask && getCurrentTask() == null) {
				getProvider().log("Not current task, finding next one! " + task.scriptName() + " "
						+ task.requiredConfigQuestStep() + " " + getQuest().getQuestProgress() + " "
						+ getQuest().getQuestStageStep() + " " + questStepRequired);
				// Not this task, next one
				continue;
			}

			// Sometimes a task can't be found and will try to correct itself afterwards,
			// but this means that he task could still be null, this way it won't break
			if (getCurrentTask() == null) {
				getProvider().log("System couldnt find a next action, logging out");
				break;
			}
			if (task.requiredConfigQuestStep() == getQuest().getQuestProgress()
					&& (questStepRequired == getQuest().getQuestStageStep()
							|| questStepRequired == getQuest().getQuestStageStep() - 1
							|| (currentTask - lastTask) > 30000)) {

				// Waiting for task to finish
				getProvider().log("finish: " + getCurrentTask().finished());

				// Waiting on task to get finished
				while (!getCurrentTask().finished()) {

					// Sometimes dialogue pops up without a dialoguetask and could get stuck because
					// of this
					if (getProvider().getDialogues().isPendingContinuation()) {
						getProvider().getDialogues().clickContinue();
					} else {
						getCurrentTask().loop();
					}

					// Sometimes the script can't perform the task correctly and will get stuck
					// performing the task over and over again without completing it
					taskAttempts++;
					if (taskAttempts > 50) {
						DatabaseUtilities.updateAccountStatusInDatabase(getProvider(), "TASK_TIMEOUT",
								getEvent().getUsername());
						BotCommands.killProcess(getScript());
					}

					getProvider().log("performing task" + getCurrentTask().getClass().getSimpleName() + " attempt: "
							+ taskAttempts);
					Thread.sleep(1000, 1500);
				}

				// Task is finished, go to the next one
				getProvider().log("On next task: " + task.scriptName());
				setCurrentTask(task);

				// Fished the last task at...? If the task doesnt want to complete, it will be
				// set 'TIMEOUT' in the database, and you will have to manually fix this. The
				// script will try to fix itself on the next run with the quest progress, but
				// can't always do this
				lastTask = System.currentTimeMillis();

				// Updating stage in database
				if (getEvent() != null && getEvent().getUsername() != null) {
					DatabaseUtilities.updateStageProgress(getProvider(), getQuest().getStage().name(),
							getQuest().getQuestStageStep(), getEvent().getUsername());
				}

				// Step increased with 1 in database
				getQuest().setQuestStageStep(questStepRequired + 1);
			}
		}
	}

	public HashMap<Integer, Task> getTasks() {
		return tasks;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setTasks(HashMap<Integer, Task> tasks) {
		this.tasks = tasks;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	/**
	 * @return the quest
	 */
	public QuestStep getQuest() {
		return quest;
	}

	/**
	 * @param quest
	 *            the quest to set
	 */
	public void setQuest(QuestStep quest) {
		this.quest = quest;
	}

	/**
	 * @return the provider
	 */
	public MethodProvider getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(MethodProvider provider) {
		this.provider = provider;
	}

	/**
	 * @return the event
	 */
	public LoginEvent getEvent() {
		return event;
	}

	/**
	 * @param event
	 *            the event to set
	 */
	public void setEvent(LoginEvent event) {
		this.event = event;
	}

	/**
	 * @return the events
	 */
	public MandatoryEventsExecution getEvents() {
		return events;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public void setEvents(MandatoryEventsExecution events) {
		this.events = events;
	}

	/**
	 * @return the script
	 */
	public Script getScript() {
		return script;
	}

	/**
	 * @param script
	 *            the script to set
	 */
	public void setScript(Script script) {
		this.script = script;
	}

}
