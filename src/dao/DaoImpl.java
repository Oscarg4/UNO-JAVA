package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Card;
import model.Player;

public class DaoImpl implements Dao {

	public static final String SCHEMA_NAME = "P1_JDBC";
    public static final String ABC = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    public static final String CONNECTION =
            "jdbc:mysql://localhost:3306/" +
                    SCHEMA_NAME +
                    ABC;
 
    private Connection connSQL;
    private final static String HOST = "127.0.0.1";
    private final static Integer PORT = 3306;
    private final static String DBNAME = "P1_JDBC";
    private final static String DBUSER = "root";
    private final static String DBPWD = "oscar2004";
 
    @Override
    public void connect() throws SQLException {
        try {
            System.out.println("Estableciendo conexion...");
            
            connSQL = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DBNAME + ABC, DBUSER, DBPWD);
            System.out.println("CONEXION ESTABLECIDA!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public void disconnect() throws SQLException {
        // Cierra la conexión si está abierta
        if (connSQL != null && !connSQL.isClosed()) {
            connSQL.close();
            System.out.println("CONEXION CERRADA!");
        }
    }

    @Override
    public int getLastIdCard(int playerId) throws SQLException {
        int lastId = 0;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT IFNULL(MAX(id), 0) + 1 AS last_id FROM card WHERE id_player = ?")) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    lastId = resultSet.getInt("last_id");
                }
            }
        }
        return lastId;
    }

    @Override
    public Card getLastCard() throws SQLException {
        Card lastCard = null;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM card WHERE id = (SELECT id_card FROM game ORDER BY id DESC LIMIT 1)")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    lastCard = new Card(
                            resultSet.getInt("id"),
                            resultSet.getString("number"),
                            resultSet.getString("color"),
                            resultSet.getInt("id_player")
                    );
                }
            }
        }
        return lastCard;
    }

    @Override
    public Player getPlayer(String user, String pass) throws SQLException {
        Player player = null;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM player WHERE user = ? AND password = ?")) {
            statement.setString(1, user);
            statement.setString(2, pass);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    player = new Player(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getInt("games"),
                            resultSet.getInt("victories")
                    );
                }
            }
        }
        return player;
    }

    @Override
    public ArrayList<Card> getCards(int playerId) throws SQLException {
        ArrayList<Card> playerCards = new ArrayList<>();
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM card WHERE id_player = ?")) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Card card = new Card(
                            resultSet.getInt("id"),
                            resultSet.getString("number"),
                            resultSet.getString("color"),
                            playerId
                    );
                    playerCards.add(card);
                }
            }
        }
        return playerCards;
    }

    @Override
    public Card getCard(int cardId) throws SQLException {
        Card card = null;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM card WHERE id = ?")) {
            statement.setInt(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    card = new Card(
                            resultSet.getInt("id"),
                            resultSet.getString("number"),
                            resultSet.getString("color"),
                            resultSet.getInt("id_player")
                    );
                }
            }
        }
        return card;
    }

    @Override
    public void saveGame(Card card) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("INSERT INTO game (id_card) VALUES (?)")) {
            statement.setInt(1, card.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void saveCard(Card card) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("INSERT INTO card (id, id_player, number, color) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, card.getId());
            statement.setInt(2, card.getPlayerId());
            statement.setString(3, card.getNumber());
            statement.setString(4, card.getColor());
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteCard(Card card) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("DELETE FROM game WHERE id_card = ?")) {
            statement.setInt(1, card.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void clearDeck(int playerId) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("DELETE FROM card WHERE id_player = ?")) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

    @Override
    public void addVictories(int playerId) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("UPDATE player SET victories = victories + 1 WHERE id = ?")) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

    @Override
    public void addGames(int playerId) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("UPDATE player SET games = games + 1 WHERE id = ?")) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

}
