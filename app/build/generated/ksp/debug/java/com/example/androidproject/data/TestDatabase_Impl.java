package com.example.androidproject.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TestDatabase_Impl extends TestDatabase {
  private volatile TestDao _testDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `test_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL, `iperfServer` TEXT NOT NULL, `testDuration` INTEGER NOT NULL, `apLatitude` REAL NOT NULL, `apLongitude` REAL NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `testCount` INTEGER NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `test_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `distanceFromAp` REAL NOT NULL, `speedMbps` REAL NOT NULL, `testDurationSeconds` REAL NOT NULL, `dataMB` REAL NOT NULL, `azimuth` REAL NOT NULL, `pitch` REAL NOT NULL, `roll` REAL NOT NULL, `accuracy` REAL NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `test_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd3e70a6e32cf992135ee83541af3cb61')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `test_sessions`");
        _db.execSQL("DROP TABLE IF EXISTS `test_results`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        _db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsTestSessions = new HashMap<String, TableInfo.Column>(9);
        _columnsTestSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("iperfServer", new TableInfo.Column("iperfServer", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("testDuration", new TableInfo.Column("testDuration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("apLatitude", new TableInfo.Column("apLatitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("apLongitude", new TableInfo.Column("apLongitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestSessions.put("testCount", new TableInfo.Column("testCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTestSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTestSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTestSessions = new TableInfo("test_sessions", _columnsTestSessions, _foreignKeysTestSessions, _indicesTestSessions);
        final TableInfo _existingTestSessions = TableInfo.read(_db, "test_sessions");
        if (! _infoTestSessions.equals(_existingTestSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "test_sessions(com.example.androidproject.data.TestSession).\n"
                  + " Expected:\n" + _infoTestSessions + "\n"
                  + " Found:\n" + _existingTestSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsTestResults = new HashMap<String, TableInfo.Column>(13);
        _columnsTestResults.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("distanceFromAp", new TableInfo.Column("distanceFromAp", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("speedMbps", new TableInfo.Column("speedMbps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("testDurationSeconds", new TableInfo.Column("testDurationSeconds", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("dataMB", new TableInfo.Column("dataMB", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("azimuth", new TableInfo.Column("azimuth", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("pitch", new TableInfo.Column("pitch", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("roll", new TableInfo.Column("roll", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("accuracy", new TableInfo.Column("accuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTestResults = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTestResults.add(new TableInfo.ForeignKey("test_sessions", "CASCADE", "NO ACTION",Arrays.asList("sessionId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTestResults = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTestResults = new TableInfo("test_results", _columnsTestResults, _foreignKeysTestResults, _indicesTestResults);
        final TableInfo _existingTestResults = TableInfo.read(_db, "test_results");
        if (! _infoTestResults.equals(_existingTestResults)) {
          return new RoomOpenHelper.ValidationResult(false, "test_results(com.example.androidproject.data.TestResult).\n"
                  + " Expected:\n" + _infoTestResults + "\n"
                  + " Found:\n" + _existingTestResults);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d3e70a6e32cf992135ee83541af3cb61", "72e8fffe4f24fda36cf2ee743c87a467");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "test_sessions","test_results");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `test_sessions`");
      _db.execSQL("DELETE FROM `test_results`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TestDao.class, TestDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public TestDao testDao() {
    if (_testDao != null) {
      return _testDao;
    } else {
      synchronized(this) {
        if(_testDao == null) {
          _testDao = new TestDao_Impl(this);
        }
        return _testDao;
      }
    }
  }
}
