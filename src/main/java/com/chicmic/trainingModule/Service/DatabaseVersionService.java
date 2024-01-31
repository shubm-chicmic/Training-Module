package com.chicmic.trainingModule.Service;

import com.chicmic.trainingModule.Repository.DbVesionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.chicmic.trainingModule.Entity.DbVersion;

@Service
@RequiredArgsConstructor
public class DatabaseVersionService {

    private static final int INITIAL_VERSION = 1;

    private final DbVesionRepo dbVesionRepo;

    public DbVersion saveDbVersion() {
        List<DbVersion> dbVersions = dbVesionRepo.findAll();

        if (dbVersions.isEmpty()) {
            // If the database is empty, create a new document with version 1
            DbVersion dbVersion = new DbVersion();
            dbVersion.setVersion(INITIAL_VERSION);
            dbVersion.setCreateTimestamp(LocalDateTime.now());
            dbVersion.setUpdateTimestamp(LocalDateTime.now());
            return dbVesionRepo.save(dbVersion);
        } else {
            // If the database is not empty, return the existing document
            return dbVersions.get(0);
        }
    }

    public DbVersion updateDatabaseVersion() {
        List<DbVersion> dbVersions = dbVesionRepo.findAll();

        if (!dbVersions.isEmpty()) {
            // If the database is not empty, update the version of all existing documents by adding 1
            dbVersions.forEach(dbVersion -> {
                dbVersion.setVersion(dbVersion.getVersion() + 1);
                dbVersion.setUpdateTimestamp(LocalDateTime.now());
            });
            dbVersions = dbVesionRepo.saveAll(dbVersions);
            // Return the first updated DbVersion, assuming you are interested in the first one
            return dbVersions.isEmpty() ? null : dbVersions.get(0);
        }
        // If the database is empty, do nothing as there are no documents to update
        return null;
    }


    public DbVersion getDatabaseVersion() {
        List<DbVersion> dbVersions = dbVesionRepo.findAll();
        return dbVersions.isEmpty() ? null : dbVersions.get(0);
    }
}
