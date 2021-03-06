package org.liesel.room1;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RepoDao {

    @Query("SELECT * FROM repo")
    LiveData<List<Repo>> getAllRepos();

    @Insert
    void insert(Repo... repos);

    @Update
    void update(Repo... repos);

    @Delete
    void delete(Repo... repos);
}