package edu.k2htm.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import edu.k2htm.datahelper.Place;
import edu.k2htm.datahelper.PlaceHelper;
import edu.k2htm.datahelper.CheckUserHelper;
import edu.k2htm.datahelper.Comment;
import edu.k2htm.datahelper.CommentHelper;
import edu.k2htm.datahelper.Report;
import edu.k2htm.datahelper.ReportGetHelper;
import edu.k2htm.datahelper.User;
import edu.k2htm.datahelper.VoteHelper;
import edu.k2htm.datahelper.VoteSetGetter;
import edu.k2htm.log.Log;

public class DatabaseConnection implements PlaceHelper, CheckUserHelper,
		VoteHelper, ReportGetHelper, CommentHelper {
	public static final String TAG = "DatabaseConnection";
	private static final String DB_URL = "jdbc:mysql://127.0.0.1/test";
	private static final String DB_USERNAME = "root";
	private static final String DB_PASSWORD = "minhminh";
	private Connection connection;
	private String dbUrl;
	private String username;
	private String password;

	public DatabaseConnection(String dbUrl, String username, String password,
			String dbName) {
		this.dbUrl = "jdbc:mysql://" + dbUrl + "/" + dbName+"?useUnicode=true&characterEncoding=utf8";
		this.username = username;
		this.password = password;

	}

	public void initTable() {

		System.out.println(TAG + ":Init DB");
		try {

			Statement statement = connection.createStatement();
			String createTable = User.getCreateTableQuery();
			statement.execute(createTable);
			System.out.println(TAG + ":create table" + User.DB_USER_TABLENAME);

			createTable = Place.getCreateTableQuery();
			statement.execute(createTable);
			System.out.println(TAG + ":create table"
					+ Place.DB_PLACE_TABLENAME);
			System.out.println(TAG + ":Init DB OK");

			statement = connection.createStatement();
			createTable = VoteSetGetter.getCreateTableQuery();
			statement.execute(createTable);
			System.out.println(TAG + ":create table"
					+ VoteSetGetter.DB_VOTE_TABLENAME);

			statement = connection.createStatement();
			createTable = Comment.getCreateTableQuery();
			statement.execute(createTable);
			System.out.println(TAG + ":create table"
					+ Comment.DB_COMMENT_TABLENAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}

	}

	public Connection initConnection(String dbUrl, String username,
			String password) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		return DriverManager.getConnection(dbUrl, username, password);
	}

	public void close() {
		Log.i(TAG, "Close");
		try {
			this.connection.close();
		} catch (Exception e) {

		}
	}

	@Override
	public void init() throws Exception {
		Log.i(TAG, "Init");
		if (dbUrl == null)
			dbUrl = DB_URL;
		if (username == null)
			username = DB_USERNAME;
		if (password == null)
			password = DB_PASSWORD;
		connection = initConnection(dbUrl, username, password);

	}

	public String getDbUrl() {
		return dbUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public void report(String name, String username, short type, long time, int lat,
			int lng, File image, String comment) throws Exception {
		Log.i(TAG, "Insert report:" + lat + ":" + lng);
		PreparedStatement preparedStatement = connection
				.prepareStatement("INSERT INTO " + Place.DB_PLACE_TABLENAME
						+ "(" + "`" + Place.DB_PLACE_NAME_COL + "`,"
						+ "`" + Place.DB_PLACE_USERNAME_COL + "`,"
						+ "`" + Place.DB_PLACE_LAT_COL + "`," + "`"
						+ Place.DB_PLACE_LNG_COL + "`," + "`"
						+ Place.DB_PLACE_TYPE_COL + "`," + "`"
						+ Place.DB_PLACE_DESCRIPTION_COL + "`," + "`"
						+ Place.DB_PLACE_TIME_COL + "`," + "`"
						+ Place.DB_PLACE_IMAGE_COL + "`)"
						+ "VALUES(?,?,?,?,?,?,?,?);");
		preparedStatement.setString(1, name);
		preparedStatement.setString(2, username);
		preparedStatement.setInt(3, lat);
		preparedStatement.setInt(4, lng);
		preparedStatement.setShort(5, type);
		preparedStatement.setString(6, comment);
		preparedStatement.setLong(7, time);
		preparedStatement.setString(8, image == null ? ""
				: (ServletListener.IMAGES_FOLDER + "/" + image.getName()));
		Log.i(TAG, preparedStatement.toString());
		preparedStatement.execute();

	}

	@Override
	public boolean checkUser(String username, String password) throws Exception {
		Log.i(TAG, "checkUser: " + username + "_" + password);
		String query = "SELECT * FROM " + User.DB_USER_TABLENAME + " WHERE "
				+ User.DB_USER_USERNAME_COL + "=? AND "
				+ User.DB_USER_PASSWORD_COL + "=?;";
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setString(1, username);
		preparedStatement.setString(2, password);
		return preparedStatement.executeQuery().next();

	}

	@Override
	public boolean register(String username, String password) throws Exception {
		Log.i(TAG, "Register: " + username + "_" + password);
		String query = "INSERT INTO " + User.DB_USER_TABLENAME + "(`"
				+ User.DB_USER_USERNAME_COL + "`,`" + User.DB_USER_PASSWORD_COL
				+ "`)VALUES(?,?)";
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setString(1, username);
		preparedStatement.setString(2, password);
		preparedStatement.execute();
		return true;
	}

	@Override
	public int[] getVote(int placeID) throws Exception {
		Log.i(TAG, "getVote:" + placeID);
		int[] result = new int[2];
		result[0] = result[1] = 0;
		Log.d(TAG, "GetVote:" + result[0] + ":" + result[1]);
		ResultSet resultSet;
		String query = "SELECT COUNT(*) AS COUNT FROM "
				+ VoteSetGetter.DB_VOTE_TABLENAME + " WHERE "
				+ VoteSetGetter.DB_VOTE_PLACE + "=? AND "
				+ VoteSetGetter.DB_VOTE_TYPE_COL + "=?;";
		Log.d(TAG, query);
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setInt(1, placeID);
		preparedStatement.setBoolean(2, true);
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next())
			result[0] = resultSet.getInt("COUNT");

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, placeID);
		preparedStatement.setBoolean(2, false);
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next())
			result[1] = resultSet.getInt("COUNT");
		Log.d(TAG, "GetVote finished:" + result[0] + ":" + result[1]);
		return result;
	}

	@Override
	public void vote(int placeID, String username, boolean bonus)
			throws Exception {
		Log.i(TAG, " Vote :" + username + ":" + placeID + ":" + bonus);
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		String query = "SELECT * FROM " + VoteSetGetter.DB_VOTE_TABLENAME
				+ " WHERE " + VoteSetGetter.DB_VOTE_PLACE + "=? AND "
				+ VoteSetGetter.DB_VOTE_VOTER_COL + "=?;";
		preparedStatement=connection.prepareStatement(query);
		preparedStatement.setInt(1, placeID);
		preparedStatement.setString(2, username);
		resultSet=preparedStatement.executeQuery();
		if(resultSet.next()){
			if(resultSet.getBoolean(VoteSetGetter.DB_VOTE_TYPE_COL)!=bonus){
				Log.d(TAG,"Change vote:"+bonus);
				query="UPDATE "+VoteSetGetter.DB_VOTE_TABLENAME+" SET "+VoteSetGetter.DB_VOTE_TYPE_COL+"=? WHERE "+VoteSetGetter.DB_VOTE_PLACE+"=? AND "+VoteSetGetter.DB_VOTE_VOTER_COL+"=?;";
				preparedStatement=connection.prepareStatement(query);
				preparedStatement.setBoolean(1, bonus);
				preparedStatement.setInt(2, placeID);
				preparedStatement.setString(3, username);
				preparedStatement.executeUpdate();
			}
			else{
				query="DELETE FROM "+VoteSetGetter.DB_VOTE_TABLENAME+" WHERE "+VoteSetGetter.DB_VOTE_PLACE+"=? AND "+VoteSetGetter.DB_VOTE_VOTER_COL+"=?;";
				preparedStatement=connection.prepareStatement(query);
				preparedStatement.setInt(1, placeID);
				preparedStatement.setString(2, username);
				preparedStatement.executeUpdate();
			}
			return;
		}
		query = "INSERT INTO " + VoteSetGetter.DB_VOTE_TABLENAME + "(`"
				+ VoteSetGetter.DB_VOTE_PLACE + "`,`"
				+ VoteSetGetter.DB_VOTE_VOTER_COL + "`,`"
				+ VoteSetGetter.DB_VOTE_TYPE_COL + "`) VALUES(?,?,?);";
		preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setInt(1, placeID);
		preparedStatement.setString(2, username);
		preparedStatement.setBoolean(3, bonus);
		preparedStatement.execute();

	}

	@Override
	public void send(String username, int placeID, String comment)
			throws Exception {
		Log.i(TAG, "Send comment:" + username + "_" + placeID + "_" + comment);
		String query = "INSERT INTO " + Comment.DB_COMMENT_TABLENAME + "(`"
				+ Comment.DB_COMMENT_COMMENTER_COL + "`,`"
				+ Comment.DB_COMMENT_PLACE_COL + "`,`"
				+ Comment.DB_COMMENT_COMMENT_COL + "`,`"
				+ Comment.DB_COMMENT_TIME_COL + "`)VALUES(?,?,?,?)";
		Log.d(TAG, query);
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setString(1, username);
		preparedStatement.setInt(2, placeID);
		preparedStatement.setString(3, comment);
		preparedStatement.setLong(4, new Date().getTime());
		preparedStatement.execute();
	}

	@Override
	public ArrayList<Comment> getComments(int placeID) throws Exception {
		Log.i(TAG, "Get comment:" + placeID);
		ArrayList<Comment> comments = new ArrayList<Comment>();
		String query = "SELECT * FROM " + Comment.DB_COMMENT_TABLENAME
				+ " WHERE " + Comment.DB_COMMENT_PLACE_COL + "=? ORDER BY "
				+ Comment.DB_COMMENT_TIME_COL + " ASC;";
		Log.d(TAG, query);
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		preparedStatement.setInt(1, placeID);
		Log.d(TAG, preparedStatement.toString());
		ResultSet resultSet = preparedStatement.executeQuery();
		Comment tmp;
		while (resultSet.next()) {
			// Log.d(TAG, "next");
			tmp = new Comment();
			tmp.setPlaceID(placeID);
			tmp.setCommenter(resultSet
					.getString(Comment.DB_COMMENT_COMMENTER_COL));
			tmp.setComment(resultSet.getString(Comment.DB_COMMENT_COMMENT_COL));
			tmp.setTime(resultSet.getLong(Comment.DB_COMMENT_TIME_COL));
			comments.add(tmp);
		}
		Log.d(TAG, comments.size() + "!");
		return comments;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ArrayList<Report> getReport(int periodMin) throws Exception {
		
		ArrayList<Report> reports = new ArrayList<Report>();
		String query = "SELECT * FROM " + Place.DB_PLACE_TABLENAME
				+ " WHERE " + Place.DB_PLACE_TIME_COL + " >=?;";
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		Log.d(TAG,"Time to get:"+new Date(new Date().getTime() -( (long)periodMin) * 60 * 1000).toLocaleString());
		preparedStatement.setLong(1, periodMin < 0 ? 0	: (new Date().getTime() - ( (long)periodMin) * 60 * 1000));
		Log.d(TAG, preparedStatement.toString());
		ResultSet resultSet = preparedStatement.executeQuery();
		int[] tmpUD = new int[2];
		while (resultSet.next()) {
			Report report = new Report();
			report.setPlaceID(resultSet.getInt(Place.DB_PLACE_ID_COL));
			report.setName(resultSet.getString(Place.DB_PLACE_NAME_COL));
			report.setUsername(resultSet
					.getString(Place.DB_PLACE_USERNAME_COL));
			report.setDescription(resultSet
					.getString(Place.DB_PLACE_DESCRIPTION_COL));
			report.setImage(resultSet.getString(Place.DB_PLACE_IMAGE_COL));
			report.setLat(resultSet.getInt(Place.DB_PLACE_LAT_COL));
			report.setLng(resultSet.getInt(Place.DB_PLACE_LNG_COL));
			report.setTime(resultSet.getLong(Place.DB_PLACE_TIME_COL));
			report.setType(resultSet.getShort(Place.DB_PLACE_TYPE_COL));
			tmpUD = this.getVote(report.getPlaceID());
			report.setVoteUp(tmpUD[0]);
			report.setVoteDown(tmpUD[1]);
			Log.d(TAG,report.getPlaceID()+":"+new Date(report.getTime()).toString());
			reports.add(report);
		}
		Log.d(TAG, "Report size:" + reports.size());
		return reports;
	}

}
