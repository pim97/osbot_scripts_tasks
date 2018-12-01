package osbot_scripts.qp7.progress;

import java.awt.Color;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

import osbot_scripts.bot.utils.Coordinates;
import osbot_scripts.database.DatabaseUtilities;
import osbot_scripts.events.CachedWidget;
import osbot_scripts.events.LoginEvent;
import osbot_scripts.events.MandatoryEventsExecution;
import osbot_scripts.framework.AccountStage;

public class ThreadDemo extends MethodProvider implements Runnable {

	private boolean run = true;

	private LoginEvent loginEvent;

	public LoginEvent getLoginEvent() {
		return loginEvent;
	}

	public void setLoginEvent(LoginEvent loginEvent) {
		this.loginEvent = loginEvent;
	}

	private boolean isWrongEmail() {
		// return getColorPicker().isColorAt(422, 232, new Color(255, 255, 0));
		return getColorPicker().isColorAt(541, 216, new Color(255, 255, 0));
	}

	// private final CachedWidget isDeadInterface = new CachedWidget("Never show me
	// this again");

	@Override
	public void run() {
		while (run) {
			try {
				if (loginEvent == null) {
					run = false;
					log("Didnt have a login event, exiting this thread");
					return;
				}

				if (loginEvent.getScript() != null) {
					log("Seperate thread is currently running.. " + loginEvent.getScript());
				}

				if (getClient().isLoggedIn() && loginEvent.hasFinished()
						&& loginEvent.getScript() != null && !loginEvent.getScript().equalsIgnoreCase("TUT_ISLAND")) {
					MandatoryEventsExecution ev = new MandatoryEventsExecution(this);
					ev.fixedMode();
					ev.fixedMode2();
					// ev.executeAllEvents();
				}

				if (!getClient().isLoggedIn() && isWrongEmail()) {
					log("Account password is wrong, setting to invalid password");
					DatabaseUtilities.updateAccountStatusInDatabase(this, "INVALID_PASSWORD",
							getLoginEvent().getUsername());
					System.exit(1);
				}

				if (!getClient().isLoggedIn() && loginEvent.hasFinished()) {
					log("Isn't logged in!?");
					Thread.sleep(5000);
					System.exit(1);
				}

				// if (isDeadInterface != null && getWidgets() != null &&
				// isDeadInterface.get(getWidgets()).isPresent()) {
				// isDeadInterface.get(getWidgets()).get().interact();
				// }

				if (getWidgets() != null && getClient().isLoggedIn()) {
					RS2Widget close = getWidgets().get(153, 71);
					if (close != null && close.isVisible()) {
						close.interact();
					}

					RS2Widget close1 = getWidgets().get(153, 107);
					if (close1 != null && close1.isVisible()) {
						close1.interact();
					}

					RS2Widget close3 = getWidgets().get(153, 108);
					if (close3 != null && close3.isVisible()) {
						close3.interact();
					}
				}

				Thread.sleep(2_000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		run = false;
	}
}