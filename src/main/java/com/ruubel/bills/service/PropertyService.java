package com.ruubel.bills.service;

import com.ruubel.bills.model.Property;
import com.ruubel.bills.model.User;
import com.ruubel.bills.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private PropertyRepository propertyRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> findAllByUser(User user) {
        return propertyRepository.findAllByUser(user);
    }
}
