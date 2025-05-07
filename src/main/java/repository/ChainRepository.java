package repository;

import model.Chain;
import org.example.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChainRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChainRepository.class);

    public List<Chain> findAll() {
        List<Chain> chains = new ArrayList<>();
        String sql = "SELECT * FROM lant ORDER BY nume";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Chain chain = mapResultSetToChain(rs);
                chains.add(chain);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all hotel chains", e);
        }

        return chains;
    }

    public Optional<Chain> findById(Long id) {
        String sql = "SELECT * FROM lant WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToChain(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching hotel chain with id: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Chain chain) {
        String sql = "INSERT INTO lant (nume) VALUES (?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chain.getName());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving hotel chain: " + chain.getName(), e);
        }

        return null;
    }

    public boolean update(Chain chain) {
        String sql = "UPDATE lant SET nume = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chain.getName());
            stmt.setLong(2, chain.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating hotel chain with id: " + chain.getId(), e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM lant WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting hotel chain with id: " + id, e);
            return false;
        }
    }

    private Chain mapResultSetToChain(ResultSet rs) throws SQLException {
        Chain chain = new Chain();
        chain.setId(rs.getLong("id"));
        chain.setName(rs.getString("nume"));
        return chain;
    }
}