package service;

import model.Location;
import repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private final LocationRepository locationRepository;

    public LocationService() {
        this.locationRepository = new LocationRepository();
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public boolean addLocation(Location location) {
        try {
            Long id = locationRepository.save(location);
            if (id != null) {
                location.setId(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error adding location: " + location.getCity() + ", " + location.getCountry(), e);
            return false;
        }
    }

    public boolean updateLocation(Location location) {
        try {
            return locationRepository.update(location);
        } catch (Exception e) {
            logger.error("Error updating location: " + location.getCity() + ", " + location.getCountry(), e);
            return false;
        }
    }

    public boolean deleteLocation(Long id) {
        try {
            return locationRepository.delete(id);
        } catch (Exception e) {
            logger.error("Error deleting location with id: " + id, e);
            return false;
        }
    }
}