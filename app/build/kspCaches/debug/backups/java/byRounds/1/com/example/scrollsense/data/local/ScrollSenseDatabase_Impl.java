package com.example.scrollsense.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.scrollsense.data.local.dao.InterventionDao;
import com.example.scrollsense.data.local.dao.InterventionDao_Impl;
import com.example.scrollsense.data.local.dao.ReflectionDao;
import com.example.scrollsense.data.local.dao.ReflectionDao_Impl;
import com.example.scrollsense.data.local.dao.UsageRecordDao;
import com.example.scrollsense.data.local.dao.UsageRecordDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScrollSenseDatabase_Impl extends ScrollSenseDatabase {
  private volatile UsageRecordDao _usageRecordDao;

  private volatile InterventionDao _interventionDao;

  private volatile ReflectionDao _reflectionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `usage_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochMillis` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `foregroundMinutes` INTEGER NOT NULL, `estimatedVideos` INTEGER NOT NULL, `lateNightMinutes` INTEGER NOT NULL, `longestSessionMinutes` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usage_records_dateEpochMillis_packageName` ON `usage_records` (`dateEpochMillis`, `packageName`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `intervention_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `usedMinutes` INTEGER NOT NULL, `limitMinutes` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reflection_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `triggerReason` TEXT NOT NULL, `mood` TEXT NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8b9735f618782849c6b09f5bfe30a732')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `usage_records`");
        db.execSQL("DROP TABLE IF EXISTS `intervention_events`");
        db.execSQL("DROP TABLE IF EXISTS `reflection_records`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsageRecords = new HashMap<String, TableInfo.Column>(7);
        _columnsUsageRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageRecords.put("dateEpochMillis", new TableInfo.Column("dateEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageRecords.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageRecords.put("foregroundMinutes", new TableInfo.Column("foregroundMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageRecords.put("estimatedVideos", new TableInfo.Column("estimatedVideos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageRecords.put("lateNightMinutes", new TableInfo.Column("lateNightMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageRecords.put("longestSessionMinutes", new TableInfo.Column("longestSessionMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsageRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsageRecords = new HashSet<TableInfo.Index>(1);
        _indicesUsageRecords.add(new TableInfo.Index("index_usage_records_dateEpochMillis_packageName", true, Arrays.asList("dateEpochMillis", "packageName"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoUsageRecords = new TableInfo("usage_records", _columnsUsageRecords, _foreignKeysUsageRecords, _indicesUsageRecords);
        final TableInfo _existingUsageRecords = TableInfo.read(db, "usage_records");
        if (!_infoUsageRecords.equals(_existingUsageRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "usage_records(com.example.scrollsense.data.local.entities.UsageRecordEntity).\n"
                  + " Expected:\n" + _infoUsageRecords + "\n"
                  + " Found:\n" + _existingUsageRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsInterventionEvents = new HashMap<String, TableInfo.Column>(5);
        _columnsInterventionEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("usedMinutes", new TableInfo.Column("usedMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("limitMinutes", new TableInfo.Column("limitMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInterventionEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInterventionEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInterventionEvents = new TableInfo("intervention_events", _columnsInterventionEvents, _foreignKeysInterventionEvents, _indicesInterventionEvents);
        final TableInfo _existingInterventionEvents = TableInfo.read(db, "intervention_events");
        if (!_infoInterventionEvents.equals(_existingInterventionEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "intervention_events(com.example.scrollsense.data.local.entities.InterventionEntity).\n"
                  + " Expected:\n" + _infoInterventionEvents + "\n"
                  + " Found:\n" + _existingInterventionEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsReflectionRecords = new HashMap<String, TableInfo.Column>(6);
        _columnsReflectionRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReflectionRecords.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReflectionRecords.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReflectionRecords.put("triggerReason", new TableInfo.Column("triggerReason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReflectionRecords.put("mood", new TableInfo.Column("mood", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReflectionRecords.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReflectionRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReflectionRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReflectionRecords = new TableInfo("reflection_records", _columnsReflectionRecords, _foreignKeysReflectionRecords, _indicesReflectionRecords);
        final TableInfo _existingReflectionRecords = TableInfo.read(db, "reflection_records");
        if (!_infoReflectionRecords.equals(_existingReflectionRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "reflection_records(com.example.scrollsense.data.local.entities.ReflectionEntity).\n"
                  + " Expected:\n" + _infoReflectionRecords + "\n"
                  + " Found:\n" + _existingReflectionRecords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8b9735f618782849c6b09f5bfe30a732", "1efbf3648ac642ce17e419fec41a42e9");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "usage_records","intervention_events","reflection_records");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `usage_records`");
      _db.execSQL("DELETE FROM `intervention_events`");
      _db.execSQL("DELETE FROM `reflection_records`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UsageRecordDao.class, UsageRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InterventionDao.class, InterventionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReflectionDao.class, ReflectionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UsageRecordDao usageRecordDao() {
    if (_usageRecordDao != null) {
      return _usageRecordDao;
    } else {
      synchronized(this) {
        if(_usageRecordDao == null) {
          _usageRecordDao = new UsageRecordDao_Impl(this);
        }
        return _usageRecordDao;
      }
    }
  }

  @Override
  public InterventionDao interventionDao() {
    if (_interventionDao != null) {
      return _interventionDao;
    } else {
      synchronized(this) {
        if(_interventionDao == null) {
          _interventionDao = new InterventionDao_Impl(this);
        }
        return _interventionDao;
      }
    }
  }

  @Override
  public ReflectionDao reflectionDao() {
    if (_reflectionDao != null) {
      return _reflectionDao;
    } else {
      synchronized(this) {
        if(_reflectionDao == null) {
          _reflectionDao = new ReflectionDao_Impl(this);
        }
        return _reflectionDao;
      }
    }
  }
}
