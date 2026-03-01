package com.rental.service;

import com.rental.entity.HouseFavorite;
import com.rental.repository.HouseFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
    @Autowired
    private HouseFavoriteRepository favoriteRepository;

    public boolean isFavorited(Long tenantId, Long houseId) {
        return favoriteRepository.existsByTenantIdAndHouseId(tenantId, houseId);
    }

    @Transactional
    public HouseFavorite toggleFavorite(Long tenantId, Long houseId) {
        return favoriteRepository.findByTenantIdAndHouseId(tenantId, houseId)
                .map(favorite -> {
                    favoriteRepository.delete(favorite);
                    return favorite;
                })
                .orElseGet(() -> {
                    HouseFavorite favorite = new HouseFavorite();
                    favorite.setTenantId(tenantId);
                    favorite.setHouseId(houseId);
                    return favoriteRepository.save(favorite);
                });
    }

    public Page<HouseFavorite> getFavorites(Long tenantId, Pageable pageable) {
        return favoriteRepository.findByTenantId(tenantId, pageable);
    }
}

