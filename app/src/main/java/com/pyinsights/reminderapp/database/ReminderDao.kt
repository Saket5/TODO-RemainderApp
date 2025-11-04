package com.pyinsights.reminderapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyinsights.reminderapp.models.ReminderModel

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder : ReminderModel): Long

    @Update
    suspend fun update(reminder : ReminderModel)

    @Delete()
    suspend fun delete(reminder : ReminderModel)

    @Query("Select * from reminder_table order by id Desc")
    suspend fun getReminderList() : List<ReminderModel>

    @Query("SELECT COUNT(*) FROM reminder_table")
    suspend fun getReminderCount(): Int

    @Query("SELECT * FROM reminder_table WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderModel?

}
