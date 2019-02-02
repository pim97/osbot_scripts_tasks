package osbot_scripts.database;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.osbot.rs07.script.MethodProvider;

import osbot_scripts.bot.utils.BotCommands;
import osbot_scripts.config.Config;
import osbot_scripts.events.LoginEvent;

public class DatabaseUtilities {

	public static final String SERVER_MULING_DATABASE = "server_muling";

	private static final String API_KEY = "SDFJNLKDASNFJK798283423NJASKF";

	private static final String LINK = "http://localhost:8000/osbot/api";

	public static final String DATABASE_THREAD_NAME = "DB_THREAD";

	public static boolean updateAccountStatusInDatabase(MethodProvider api, String status, String email,
			LoginEvent login) {

		boolean finish = false;

		while (!finish) {

			try {
				String query = null;
				if (isServerMuleTradingAccount(api, login, email)) {
					query = "UPDATE " + SERVER_MULING_DATABASE + ".account SET status = ? WHERE email=?";
					if (status.equalsIgnoreCase("TASK_TIMEOUT")) {
						query = "UPDATE " + SERVER_MULING_DATABASE
								+ ".account SET status = ?, amount_timeout = amount_timeout + 1 WHERE email=?";
					}
				} else {
					query = "UPDATE account SET status = ? WHERE email=?";
					if (status.equalsIgnoreCase("TASK_TIMEOUT")) {
						query = "UPDATE account SET status = ?, amount_timeout = amount_timeout + 1 WHERE email=?";
					}
				}
				Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1, status);
				preparedStmt.setString(2, email);

				// execute the java preparedstatement
				preparedStmt.executeUpdate();
				preparedStmt.close();
				conn.close();

				System.out.println("Updated account in database with new status!");
				return true;
			} catch (Exception e) {
				api.log(exceptionToString(e, api, login));
				// BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E08");
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return false;
	}

	public static boolean updateAccountValue(String database, MethodProvider api, String email, int value,
			LoginEvent login) {
		try {
			String query = "UPDATE " + database + ".account SET account_value = ? WHERE email=?";
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, Integer.toString(value));
			preparedStmt.setString(2, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new price value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E07");
			return false;
		}
	}

	public static boolean updateAccountValue(MethodProvider api, String email, int value, LoginEvent login) {
		try {
			String query = "UPDATE account SET account_value = ? WHERE email=?";
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, Integer.toString(value));
			preparedStmt.setString(2, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new price value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E07");
			return false;
		}
	}

	public static boolean updateAccountUsername(MethodProvider api, String email, String ingameName, LoginEvent login) {
		try {
			String query = null;
			if (isServerMuleTradingAccount(api, login, email)) {
				query = "UPDATE " + SERVER_MULING_DATABASE + ".account SET name = ? WHERE email=?";
			} else {
				query = "UPDATE account SET name = ? WHERE email=?";
			}
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, ingameName);
			preparedStmt.setString(2, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E06");
			return false;
		}
	}

	public static void insertLoggingMessage(MethodProvider api, LoginEvent login, String type, String message,
			String itemName) {
		// the mysql insert statement
		String query = "INSERT INTO logging.`log` (type, message, server, item) values (?,?,?,?)";

		// create the mysql insert preparedstatement
		PreparedStatement preparedStmt;
		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			preparedStmt = conn.prepareStatement(query);

			preparedStmt.setString(1, type);
			preparedStmt.setString(2, message);
			preparedStmt.setString(3, login.getDbName());
			preparedStmt.setString(4, itemName);

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// api.log(exceptionToString(e));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E06");
		}
	}

	// INSERT INTO log (`type`, `message`) values ("GOLD_TRANSFER", "a")
	public static void insertLoggingMessage(MethodProvider api, LoginEvent login, String type, String message) {
		// the mysql insert statement
		String query = "INSERT INTO logging.`log` (type, message, server) values (?,?,?)";

		// create the mysql insert preparedstatement
		PreparedStatement preparedStmt;
		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			preparedStmt = conn.prepareStatement(query);

			preparedStmt.setString(1, type);
			preparedStmt.setString(2, message);
			preparedStmt.setString(3, login.getDbName());

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// api.log(exceptionToString(e));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E06");
		}
	}

	public static boolean updateLoginStatus(String database, MethodProvider api, String email, String loginStatus,
			LoginEvent login) {
		try {
			api.log("log email and login status: " + email + " status " + loginStatus);
			String query = "UPDATE " + database + ".account SET login_status = ? WHERE email=?";
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, loginStatus);
			preparedStmt.setString(2, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E05");
			return false;
		}
	}

	public static boolean updateLoginStatus(MethodProvider api, String email, String loginStatus, LoginEvent login) {
		try {
			api.log("log email and login status: " + email + " status " + loginStatus);
			String query = "UPDATE account SET login_status = ? WHERE email=?";
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, loginStatus);
			preparedStmt.setString(2, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E05");
			return false;
		}
	}

	public static boolean updateStageProgress(String database, MethodProvider api, String accountStage, int number,
			String email, LoginEvent login) {
		try {
			String query = "UPDATE " + database
					+ ".account SET account_stage = ?, account_stage_progress = ?, trade_with_other = NULL WHERE email=?";
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, accountStage);
			preparedStmt.setInt(2, number);
			preparedStmt.setString(3, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E04");
			return false;
		}
	}

	public static boolean updateStageProgress(MethodProvider api, String accountStage, int number, String email,
			LoginEvent login) {
		try {

			String query = "";
			if (isServerMuleTradingAccount(api, login, email)) {
				query = "UPDATE server_muling.account SET account_stage = ?, account_stage_progress = ?, trade_with_other = NULL WHERE email=?";
			} else {
				query = "UPDATE account SET account_stage = ?, account_stage_progress = ?, trade_with_other = NULL WHERE email=?";
			}
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, accountStage);
			preparedStmt.setInt(2, number);
			preparedStmt.setString(3, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();

			System.out.println("Updated account in database with new value!");

			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E04");
			return false;
		}
	}

	public static boolean updateAccountBreakTill(MethodProvider api, String email, int minutesBreak, LoginEvent login) {
		try {
			if (!Config.NO_BREAK) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());

				calendar.add(Calendar.MINUTE, minutesBreak);

				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateTime = sdf.format(calendar.getTime());

				String query = "UPDATE account SET break_till = ? WHERE email=?";
				Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1, dateTime);
				preparedStmt.setString(2, email);

				// execute the java preparedstatement
				preparedStmt.executeUpdate();
				preparedStmt.close();
				conn.close();

				System.out.println("Updated account in database with new value!");
			}
			return true;

		} catch (Exception e) {
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E03");
			return false;
		}
	}

	/**
	 * Updates the account status (banned, locked etc) in the database
	 * 
	 * @param api
	 *            TODO
	 * @param login
	 *            TODO
	 * @param newPassword
	 * @param accountId
	 * 
	 * @return
	 */
	// public static boolean updateAccountStatusInDatabase(MethodProvider prov,
	// String status, String email) {
	// try {
	// if (System.currentTimeMillis() - lastStatusUpdate > 5_000) {
	// // prov.log("May only update this value once every 5 seconds " +
	// // lastStatusUpdate);
	// // return false;
	// // }
	//
	// lastStatusUpdate = System.currentTimeMillis();
	//
	// Thread t1 = new Thread(() -> {
	// try {
	// String urlParameters = "?status=" + URLEncoder.encode(status, "UTF-8") +
	// "&email="
	// + URLEncoder.encode(email, "UTF-8") + "&qp="
	// + URLEncoder.encode("" + prov.getQuests().getQuestPoints(), "UTF-8");
	//
	// prov.log(LINK + "" + urlParameters);
	// sendGet(prov, LINK + "" + urlParameters);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// });
	//
	// t1.setName(DATABASE_THREAD_NAME);
	// t1.start();
	// }
	//
	// return true;
	//
	// } catch (Exception e) {
	// prov.log(e);
	// e.printStackTrace();
	// return false;
	// }
	// }

	public static String exceptionToString(Exception e, MethodProvider api, LoginEvent login) {
		if (e == null) {
			return null;
		}
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		if (!Config.NO_LOGIN) {
			insertLoggingMessage(api, login, "ERROR", sw.toString());
		}

		return sw.toString();
	}

	public static boolean isServerMuleTradingAccount(MethodProvider api, LoginEvent login, String email) {

		String sql = "SELECT email FROM server_muling.account";
		String emailName = null;

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql);

			while (results.next()) {
				try {
					emailName = results.getString("email");

					if (emailName.equalsIgnoreCase(email)) {
						return true;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E02");
		}
		return false;
	}

	public static int getMuleTradingFreeAccounts(MethodProvider api, LoginEvent login) {
		String sql = "SELECT COUNT(*) AS COUNT FROM account INNER JOIN proxies ON proxies.ip_addres=proxy_ip WHERE account_stage=\"MULE_TRADING\" AND STATUS=\"AVAILABLE\" ORDER BY account_value DESC";
		int freeAmount = -1;

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql);

			while (results.next()) {
				try {
					freeAmount = results.getInt("count");

					api.log("mule count:  " + freeAmount);
					return freeAmount;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E02");
		}
		return -1;
	}

	public static String getAccountStatusByIngameName(String server, MethodProvider api, String ingameName,
			LoginEvent login) {
		// String sql = "SELECT trade_with_other FROM account WHERE email='" + email +
		// "'";
		String sql2 = "SELECT status FROM " + server + ".account WHERE name = '" + ingameName + "'";
		String status = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql2);

			while (results.next()) {
				try {
					status = results.getString("status");

					// api.log("trading partner found: " + partner);
					return status;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E011");
		}
		return null;
	}

	public static String getAccountStatusByIngameName(MethodProvider api, String ingameName, LoginEvent login) {
		// String sql = "SELECT trade_with_other FROM account WHERE email='" + email +
		// "'";
		String sql2 = "SELECT status FROM account WHERE name = '" + ingameName + "'";
		String status = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql2);

			while (results.next()) {
				try {
					status = results.getString("status");

					// api.log("trading partner found: " + partner);
					return status;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E011");
		}
		return null;
	}

	public static boolean accountContainsInDatabase(MethodProvider api, String name, LoginEvent login) {
		// String sql = "SELECT trade_with_other FROM account WHERE email='" + email +
		// "'";
		String sql2 = "SELECT COUNT(*) as found FROM account WHERE name = '" + name + "'";
		String found = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql2);

			while (results.next()) {
				try {
					found = results.getString("found");
					boolean inDatabase = Integer.parseInt(found) > 0 ? true : false;

					// api.log("trading partner found: " + partner);
					return inDatabase;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E012");
		}
		return false;
	}

	public static String getAccountStatus(String database, MethodProvider api, String email, LoginEvent login) {
		// String sql = "SELECT trade_with_other FROM account WHERE email='" + email +
		// "'";
		String sql2 = "SELECT status FROM " + database + ".account WHERE email = '" + email + "'";
		String status = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql2);

			while (results.next()) {
				try {
					status = results.getString("status");

					// api.log("trading partner found: " + partner);
					return status;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E011");
		}
		return null;
	}

	public static String getAccountStatus(MethodProvider api, String email, LoginEvent login) {
		// String sql = "SELECT trade_with_other FROM account WHERE email='" + email +
		// "'";
		String sql2 = "SELECT status FROM account WHERE email = '" + email + "'";
		String status = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql2);

			while (results.next()) {
				try {
					status = results.getString("status");

					// api.log("trading partner found: " + partner);
					return status;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E011");
		}
		return null;
	}

	public static String getAccountToTradeWith(MethodProvider api, String email, LoginEvent login) {
		// String sql = "SELECT trade_with_other FROM account WHERE email='" + email +
		// "'";
		String sql2 = "SELECT trade_with_other FROM account WHERE trade_with_other IS NOT NULL AND account_stage = 'UNKNOWN' AND status='MULE' AND email='"
				+ email + "'";
		String partner = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql2);

			while (results.next()) {
				try {
					partner = results.getString("trade_with_other");

					// api.log("trading partner found: " + partner);
					return partner;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E011");
		}
		return null;
	}

	public static String getScriptConfigValue(MethodProvider api, LoginEvent login) {
		String sql = "SELECT script FROM config";
		String progress = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql);

			while (results.next()) {
				try {
					progress = results.getString("script");

					api.log("quest steps: " + progress);
					return progress;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E01");
		}
		return null;
	}

	public static String getQuestProgress(MethodProvider api, String email, LoginEvent login) {
		String sql = "SELECT account_stage_progress FROM account WHERE email='" + email + "'";
		String progress = "";

		try {
			Connection conn = DatabaseConnection.getDatabase().getConnection(api, login);
			ResultSet results = conn.createStatement().executeQuery(sql);

			while (results.next()) {
				try {
					progress = results.getString("account_stage_progress");

					api.log("quest progress found: " + progress);
					return progress;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					api.log(exceptionToString(e, api, login));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			api.log(exceptionToString(e, api, login));
			BotCommands.waitBeforeKill(api, "BECAUSE AN ERROR E01");
		}
		return null;
	}

	// private static long lastUpdateAccountValue = -1;

	/**
	 * Updates the account value of an account
	 * 
	 * @param prov
	 * @param email
	 * @param value
	 * @return
	 */
	// public static boolean updateAccountValue(MethodProvider prov, String email,
	// int value) {
	// try {
	//
	// if (System.currentTimeMillis() - lastUpdateAccountValue > 20000) {
	// // prov.log("May only update this value once every 20 seconds " +
	// // lastUpdateAccountValue);
	// // return false;
	// // }
	//
	// lastUpdateAccountValue = System.currentTimeMillis();
	//
	// Thread t1 = new Thread(() -> {
	// try {
	// String urlParameters = "?email=" + URLEncoder.encode(email, "UTF-8") +
	// "&value="
	// + URLEncoder.encode(Integer.toString(value), "UTF-8");
	//
	// prov.log(LINK + "" + urlParameters);
	// sendGet(prov, LINK + "" + urlParameters);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// });
	//
	// t1.setName(DATABASE_THREAD_NAME);
	// t1.start();
	// }
	//
	// return true;
	//
	// } catch (Exception e) {
	// prov.log(e);
	// e.printStackTrace();
	// return false;
	// }
	// }

	// private static long lastUsernameUpdate = -1;

	// public static boolean updateAccountUsername(MethodProvider prov, String
	// email, String ingameName) {
	// try {
	// if (System.currentTimeMillis() - lastUsernameUpdate > 20000) {
	// // prov.log("May only update this value once every 20 seconds " +
	// // lastUsernameUpdate);
	// // return false;
	// // }
	//
	// lastUsernameUpdate = System.currentTimeMillis();
	//
	// Thread t1 = new Thread(() -> {
	// try {
	// String urlParameters = "?email=" + URLEncoder.encode(email, "UTF-8") +
	// "&ingameName="
	// + URLEncoder.encode(ingameName, "UTF-8");
	//
	// prov.log(LINK + "" + urlParameters);
	// sendGet(prov, LINK + "" + urlParameters);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// });
	//
	// t1.setName(DATABASE_THREAD_NAME);
	// t1.start();
	// }
	//
	// return true;
	//
	// } catch (Exception e) {
	// prov.log(e);
	// e.printStackTrace();
	// return false;
	// }
	// }

	// public static boolean updateLoginStatus(MethodProvider api, String email,
	// String loginStatus) {
	// try {
	//
	// Thread t1 = new Thread(() -> {
	// try {
	// String urlParameters = "?email=" + URLEncoder.encode(email, "UTF-8") +
	// "&loginstatus="
	// + URLEncoder.encode(loginStatus, "UTF-8");
	//
	// api.log(LINK + "" + urlParameters);
	// sendGet(api, LINK + "" + urlParameters);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// });
	//
	// t1.setName(DATABASE_THREAD_NAME);
	// t1.start();
	//
	// return true;
	//
	// } catch (Exception e) {
	// // prov.log(e);
	// e.printStackTrace();
	// return false;
	// }
	// }

	// public static boolean updateAccountBreakTill(MethodProvider prov, String
	// email, int minutesBreak) {
	// try {
	// if (!Config.NO_BREAK) {
	// Thread t1 = new Thread(() -> {
	// try {
	// Calendar calendar = Calendar.getInstance();
	// calendar.setTime(new Date());
	//
	// calendar.add(Calendar.MINUTE, minutesBreak);
	//
	// java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd
	// HH:mm:ss");
	// String dateTime = sdf.format(calendar.getTime());
	//
	// String urlParameters = "?email=" + URLEncoder.encode(email, "UTF-8") +
	// "&breaktill="
	// + URLEncoder.encode(dateTime, "UTF-8");
	//
	// if (prov == null) {
	// System.out.println(LINK + "" + urlParameters);
	// } else {
	// prov.log(LINK + "" + urlParameters);
	// }
	// sendGet(prov, LINK + "" + urlParameters);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// });
	//
	// t1.setName(DATABASE_THREAD_NAME);
	// t1.start();
	// return true;
	// }
	// return false;
	//
	// } catch (Exception e) {
	// prov.log(e);
	// e.printStackTrace();
	// return false;
	// }
	// }

	/**
	 * Updates stage progress for quests etc.
	 * 
	 * @param prov
	 * @param accountStatus
	 * @param number
	 * @param email
	 * @return
	 */
	// public static boolean updateStageProgress(MethodProvider prov, String
	// accountStatus, int number, String email) {
	// try {
	// Thread t1 = new Thread(() -> {
	// try {
	// String urlParameters = "?accountStage=" + URLEncoder.encode(accountStatus,
	// "UTF-8") + "&email="
	// + URLEncoder.encode(email, "UTF-8") + "&number=" + URLEncoder.encode("" +
	// number, "UTF-8");
	//
	// prov.log(LINK + "" + urlParameters);
	// sendGet(prov, LINK + "" + urlParameters);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// });
	//
	// t1.setName(DATABASE_THREAD_NAME);
	// t1.start();
	//
	// return true;
	//
	// } catch (Exception e) {
	// prov.log(e);
	// e.printStackTrace();
	// return false;
	// }
	// }

	/**
	 * 
	 * @param prov
	 * @param email
	 * @return
	 */
	// public static String getQuestProgress(MethodProvider prov, String email) {
	// try {
	//
	// String urlParameters = "?email=" + URLEncoder.encode(email, "UTF-8");
	//
	// prov.log(LINK + "/quest" + "" + urlParameters);
	//
	// StringBuilder name = new StringBuilder();
	//
	// name.append(sendGet(prov, LINK + "/quest" + "" + urlParameters));
	//
	// return name.toString();
	// } catch (Exception e) {
	// prov.log(e);
	// e.printStackTrace();
	// return null;
	// }
	// }

	// private static String sendGet(MethodProvider api, String url) throws
	// IOException {
	// String USER_AGENT = "Mozilla/5.0";
	// URL obj = new URL(url);
	// HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	// con.setRequestMethod("GET");
	// con.setRequestProperty("User-Agent", USER_AGENT);
	// int responseCode = con.getResponseCode();
	// System.out.println("GET Response Code :: " + responseCode);
	// if (responseCode == HttpURLConnection.HTTP_OK) { // success
	// BufferedReader in = new BufferedReader(new
	// InputStreamReader(con.getInputStream()));
	// String inputLine;
	// StringBuffer response = new StringBuffer();
	//
	// while ((inputLine = in.readLine()) != null) {
	// response.append(inputLine);
	// }
	// in.close();
	// // print result
	// System.out.println(response.toString());
	//
	// return response.toString();
	// } else {
	// if (api != null) {
	// api.log("GET request not worked");
	// }
	// try {
	// Thread.sleep(60_000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return null;
	// }

	private static String sendGet(MethodProvider api, String url) throws Exception {
		String USER_AGENT = "Mozilla/5.0";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		return response.toString();

	}

	public static String executePost(String targetURL, String urlParameters) {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

}
