package osbot_scripts.sections;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WalkingEvent;

import osbot_scripts.sections.total.progress.MainState;
import osbot_scripts.util.Sleep;

public class WizardGuideSection extends TutorialSection {

	public WizardGuideSection() {
		super("Magic Instructor");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onLoop() throws InterruptedException {
		// TODO Auto-generated method stub
		log(getProgress());

		switch (getProgress()) {
		case 620:
			Position walkTo = new Position(3141, 3086, 0);
			if (myPlayer().getArea(5).contains(walkTo)) {
				talkAndContinueWithInstructor();
			} else {
				if (getWalking().walk(walkTo)) {
					if (Sleep.sleepUntil(() -> myPlayer().getArea(3).contains(walkTo), 10000, 3000)) {
					}
				}
			}
			break;

		case 630:
			getTabs().open(Tab.MAGIC);
			break;
		case 640:
			talkAndContinueWithInstructor();
			break;

		case 650:
			if (!CHICKEN_AREA.contains(myPosition())) {
                walkToChickenArea();
            } else {
                attackChicken();
            }
			break;

		case 670:
			if (getDialogues().isPendingOption()) {
				getDialogues().selectOption("No, I'm not planning to do that.", "Yes.", "I'm fine, thanks.");
			} else if (getMagic().isSpellSelected()) {
				getMagic().deselectSpell();
			} else {
				talkAndContinueWithInstructor();
			}
			break;
		}

	}
	
	private boolean walkToChickenArea() {
        WalkingEvent walkingEvent = new WalkingEvent(CHICKEN_AREA);
        walkingEvent.setMinDistanceThreshold(0);
        walkingEvent.setMiniMapDistanceThreshold(0);
        execute(walkingEvent);
        return walkingEvent.hasFinished();
    }

	
    private static final Area CHICKEN_AREA = new Area(3139, 3091, 3140, 3090);

	private boolean attackChicken() {
		NPC chicken = getNpcs().closest("Chicken");
		if (chicken != null && getMagic().castSpellOnEntity(Spells.NormalSpells.WIND_STRIKE, chicken)) {
			Sleep.sleepUntil(() -> getProgress() != 670, 6000);
			return true;
		}
		return false;
	}

	@Override
	public boolean isCompleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MainState getNextMainState() {
		// TODO Auto-generated method stub
		return null;
	}

}
