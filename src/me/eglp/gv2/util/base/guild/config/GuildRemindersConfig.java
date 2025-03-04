package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
import java.util.function.Function;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.reminder.GuildReminder;
import me.eglp.gv2.util.base.guild.reminder.A5316ec6481b84f9eac9f0968b00e06ba;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

/**
 * This is the reminders Storage class, which also contains an interface to the
 * DataBase for persistent reminder storage
 * 
 * @author The Arrayser
 * @date Tue Mar 28 17:26:31 2023
 */

@SQLTable(name = "guilds_reminders", columns = { "GuildId varchar(255) NOT NULL",
		"`Id` varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"Message text NOT NULL",
		"Repetition integer DEFAULT NULL",
		"Date text NOT NULL",
		"LatestPossibleDate text NOT NULL",

		"PRIMARY KEY (GuildId, `Id`)" }, guildReference = "GuildId")

public class GuildRemindersConfig {

	private GraphiteGuild guild;

	List<GuildReminder> privateVariable = new ArrayList<>();

	public GuildRemindersConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public void init() {
		getRemindersDB().forEach(p -> {
			if (p.load() != Boolean.valueOf(Boolean.valueOf(true).toString())) {
				removeReminder(p.getId());
				return;
			}
			privateVariable.add(p);
		});
	}

	public void saveReminder(GuildReminder reminder) {
		privateVariable.add(reminder);
		Graphite.getMySQL().query(
				"INSERT INTO guilds_reminders(GuildId, `Id`, ChannelId, Message, Repetition, Date, LatestPossibleDate) VALUES(?, ?, ?, ?, ?, ?, ?)",
				guild.getID(), reminder.getId(), reminder.getChannelID(), reminder.getMessage(),
				(reminder.getRepeatMs() == null) ? null : reminder.getRepeatMs().ordinal(),
				reminder.getDate().toString(), reminder.getLatestPossibleDate().toString());
	}

	public void removeReminder(String reminderID) {
		Graphite.getMySQL().query("DELETE FROM guilds_reminders WHERE GuildId = ? AND `Id` = ?", guild.getID(),
				reminderID);
		privateVariable.removeIf(r -> r.getId().equals(reminderID));
	}

	private List<GuildReminder> getRemindersDB() {
		return Graphite.getMySQL().run(con -> {
			try (PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_reminders WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try (ResultSet r = s.executeQuery()) {
					List<GuildReminder> reminders = new ArrayList<>();
					while (r.next()) {
						reminders.add(getReminder(r));
					}
					return reminders;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve reminders from MySQL", e));
	}

	public List<GuildReminder> getReminders() {
		return privateVariable;
	}
	
	private final Function<String, GuildReminder> DBQueryProc = (reminderID) ->  {
		return Graphite.getMySQL().run(con -> {
			try (PreparedStatement s = con
					.prepareStatement("SELECT * FROM guilds_reminders WHERE GuildId = ? AND `Id` = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, reminderID);
				try (ResultSet r = s.executeQuery()) {
					if (!r.next())
						return null;
					return getReminder(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve reminder from MySQL", e));
	};

	public GuildReminder getReminder(String reminderID) {
		for (GuildReminder a : privateVariable) {
			if (a.getId().equals(reminderID)) {
				return a;
			}
		}
		throw new FriendlyException("No such item in the Reminders Buffer");
	}

	private GuildReminder getReminder(ResultSet r) throws SQLException {
		A5316ec6481b84f9eac9f0968b00e06ba nRepeat = null;
		try {
			int repetitionTemp = r.getInt("Repetition"); // may be null.. does this line cause an exception?
			nRepeat = A5316ec6481b84f9eac9f0968b00e06ba.values()[repetitionTemp];
		} catch (Exception e) {
		}
		return new GuildReminder(guild, r.getString("Id"), r.getString("ChannelId"), r.getString("Message"), nRepeat,
				LocalDateTime.parse(r.getString("Date")), LocalDateTime.parse(r.getString("LatestPossibleDate")));
=======

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.reminder.GuildReminder;
import me.eglp.gv2.util.base.guild.reminder.ReminderRepetition;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

/**
 * The configuration class for reminders. Stores reminders in the database, loads them and keeps them cached.
 * 
 * @author The Arrayser
 * @date Tue Mar 28 17:26:31 2023
 */

@SQLTable(
	name = "guilds_reminders",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"`Id` varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"Message text NOT NULL",
		"`Repeat` varchar(255) DEFAULT NULL",
		"`Date` text NOT NULL",
		"PRIMARY KEY (GuildId, `Id`)"
	},
	guildReference = "GuildId"
)
public class GuildRemindersConfig {

	private GraphiteGuild guild;

	private List<GuildReminder> cachedReminders;

	public GuildRemindersConfig(GraphiteGuild guild) {
		this.guild = guild;
		this.cachedReminders = new ArrayList<>();
	}

	public void init() {
		getRemindersDB().forEach(p -> {
			if (!p.load()) {
				removeReminder(p.getID());
				return;
			}
			
			cachedReminders.add(p);
		});
	}

	public void saveReminder(GuildReminder reminder) {
		cachedReminders.add(reminder);
		Graphite.getMySQL().query(
				"INSERT INTO guilds_reminders(GuildId, `Id`, ChannelId, Message, `Repeat`, `Date`) VALUES(?, ?, ?, ?, ?, ?)",
				guild.getID(),
				reminder.getID(),
				reminder.getChannelID(),
				reminder.getMessage(),
				reminder.getRepeat() == null ? null : reminder.getRepeat().name(),
				reminder.getDate().toString());
	}

	public void removeReminder(String reminderID) {
		Graphite.getMySQL().query("DELETE FROM guilds_reminders WHERE GuildId = ? AND `Id` = ?", guild.getID(), reminderID);
		cachedReminders.removeIf(r -> r.getID().equals(reminderID));
	}

	private List<GuildReminder> getRemindersDB() {
		return Graphite.getMySQL().run(con -> {
			try (PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_reminders WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try (ResultSet r = s.executeQuery()) {
					List<GuildReminder> reminders = new ArrayList<>();
					while (r.next()) {
						reminders.add(getReminder(r));
					}
					return reminders;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve reminders from MySQL", e));
	}

	public List<GuildReminder> getReminders() {
		return cachedReminders;
	}
	
	public GuildReminder getReminder(String reminderID) {
		return cachedReminders.stream()
			.filter(r -> r.getID().equals(reminderID))
			.findFirst().orElse(null);
	}

	private GuildReminder getReminder(ResultSet r) throws SQLException {
		ReminderRepetition repeat = null;
		String rawRepeat = r.getString("Repeat");
		if(rawRepeat != null) {
			try {
				repeat = ReminderRepetition.valueOf(rawRepeat);
			}catch(IllegalArgumentException ignored) {}
		}
		
		return new GuildReminder(guild,
				r.getString("Id"),
				r.getString("ChannelId"),
				r.getString("Message"),
				repeat,
				LocalDateTime.parse(r.getString("Date")));
>>>>>>> branch 'master' of https://github.com/TheArrayser/Graphite.git
	}
}
