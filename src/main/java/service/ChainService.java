package service;

import model.Chain;
import model.Hotel;
import repository.ChainRepository;
import repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ChainService {
    private static final Logger logger = LoggerFactory.getLogger(ChainService.class);
    private final ChainRepository chainRepository;
    private final HotelRepository hotelRepository;

    public ChainService() {
        this.chainRepository = new ChainRepository();
        this.hotelRepository = new HotelRepository();
    }

    public List<Chain> getAllChains() {
        List<Chain> chains = chainRepository.findAll();
        for (Chain chain : chains) {
            // Load hotels for each chain
            List<Hotel> hotels = hotelRepository.findByChainId(chain.getId());
            chain.setHotels(hotels);
        }
        return chains;
    }

    public Optional<Chain> getChainById(Long id) {
        Optional<Chain> chainOpt = chainRepository.findById(id);
        if (chainOpt.isPresent()) {
            Chain chain = chainOpt.get();
            // Load hotels for the chain
            List<Hotel> hotels = hotelRepository.findByChainId(chain.getId());
            chain.setHotels(hotels);
            return Optional.of(chain);
        }
        return Optional.empty();
    }

    public boolean addChain(Chain chain) {
        try {
            Long id = chainRepository.save(chain);
            if (id != null) {
                chain.setId(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error adding hotel chain: " + chain.getName(), e);
            return false;
        }
    }

    public boolean updateChain(Chain chain) {
        try {
            return chainRepository.update(chain);
        } catch (Exception e) {
            logger.error("Error updating hotel chain: " + chain.getName(), e);
            return false;
        }
    }

    public boolean deleteChain(Long id) {
        try {
            // Check if chain has hotels
            List<Hotel> hotels = hotelRepository.findByChainId(id);
            if (!hotels.isEmpty()) {
                logger.error("Cannot delete chain with id: {} because it has hotels", id);
                return false;
            }
            return chainRepository.delete(id);
        } catch (Exception e) {
            logger.error("Error deleting hotel chain with id: " + id, e);
            return false;
        }
    }
}