package com.example.androidproject.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TestDao_Impl implements TestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TestSession> __insertionAdapterOfTestSession;

  private final EntityInsertionAdapter<TestResult> __insertionAdapterOfTestResult;

  private final EntityDeletionOrUpdateAdapter<TestSession> __updateAdapterOfTestSession;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSession;

  public TestDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTestSession = new EntityInsertionAdapter<TestSession>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `test_sessions` (`id`,`description`,`iperfServer`,`testDuration`,`apLatitude`,`apLongitude`,`startTime`,`endTime`,`testCount`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TestSession value) {
        stmt.bindLong(1, value.getId());
        if (value.getDescription() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getDescription());
        }
        if (value.getIperfServer() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getIperfServer());
        }
        stmt.bindLong(4, value.getTestDuration());
        stmt.bindDouble(5, value.getApLatitude());
        stmt.bindDouble(6, value.getApLongitude());
        stmt.bindLong(7, value.getStartTime());
        if (value.getEndTime() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindLong(8, value.getEndTime());
        }
        stmt.bindLong(9, value.getTestCount());
      }
    };
    this.__insertionAdapterOfTestResult = new EntityInsertionAdapter<TestResult>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `test_results` (`id`,`sessionId`,`timestamp`,`latitude`,`longitude`,`distanceFromAp`,`speedMbps`,`testDurationSeconds`,`dataMB`,`azimuth`,`pitch`,`roll`,`accuracy`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TestResult value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getSessionId());
        stmt.bindLong(3, value.getTimestamp());
        stmt.bindDouble(4, value.getLatitude());
        stmt.bindDouble(5, value.getLongitude());
        stmt.bindDouble(6, value.getDistanceFromAp());
        stmt.bindDouble(7, value.getSpeedMbps());
        stmt.bindDouble(8, value.getTestDurationSeconds());
        stmt.bindDouble(9, value.getDataMB());
        stmt.bindDouble(10, value.getAzimuth());
        stmt.bindDouble(11, value.getPitch());
        stmt.bindDouble(12, value.getRoll());
        stmt.bindDouble(13, value.getAccuracy());
      }
    };
    this.__updateAdapterOfTestSession = new EntityDeletionOrUpdateAdapter<TestSession>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `test_sessions` SET `id` = ?,`description` = ?,`iperfServer` = ?,`testDuration` = ?,`apLatitude` = ?,`apLongitude` = ?,`startTime` = ?,`endTime` = ?,`testCount` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TestSession value) {
        stmt.bindLong(1, value.getId());
        if (value.getDescription() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getDescription());
        }
        if (value.getIperfServer() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getIperfServer());
        }
        stmt.bindLong(4, value.getTestDuration());
        stmt.bindDouble(5, value.getApLatitude());
        stmt.bindDouble(6, value.getApLongitude());
        stmt.bindLong(7, value.getStartTime());
        if (value.getEndTime() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindLong(8, value.getEndTime());
        }
        stmt.bindLong(9, value.getTestCount());
        stmt.bindLong(10, value.getId());
      }
    };
    this.__preparedStmtOfDeleteSession = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM test_sessions WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final TestSession session,
      final Continuation<? super Long> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          long _result = __insertionAdapterOfTestSession.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object insertResult(final TestResult result,
      final Continuation<? super Long> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          long _result = __insertionAdapterOfTestResult.insertAndReturnId(result);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object updateSession(final TestSession session,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTestSession.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteSession(final long sessionId, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSession.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sessionId);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfDeleteSession.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<TestSession>> getAllSessions() {
    final String _sql = "SELECT * FROM test_sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"test_sessions"}, new Callable<List<TestSession>>() {
      @Override
      public List<TestSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIperfServer = CursorUtil.getColumnIndexOrThrow(_cursor, "iperfServer");
          final int _cursorIndexOfTestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "testDuration");
          final int _cursorIndexOfApLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "apLatitude");
          final int _cursorIndexOfApLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "apLongitude");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfTestCount = CursorUtil.getColumnIndexOrThrow(_cursor, "testCount");
          final List<TestSession> _result = new ArrayList<TestSession>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final TestSession _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpIperfServer;
            if (_cursor.isNull(_cursorIndexOfIperfServer)) {
              _tmpIperfServer = null;
            } else {
              _tmpIperfServer = _cursor.getString(_cursorIndexOfIperfServer);
            }
            final int _tmpTestDuration;
            _tmpTestDuration = _cursor.getInt(_cursorIndexOfTestDuration);
            final double _tmpApLatitude;
            _tmpApLatitude = _cursor.getDouble(_cursorIndexOfApLatitude);
            final double _tmpApLongitude;
            _tmpApLongitude = _cursor.getDouble(_cursorIndexOfApLongitude);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpTestCount;
            _tmpTestCount = _cursor.getInt(_cursorIndexOfTestCount);
            _item = new TestSession(_tmpId,_tmpDescription,_tmpIperfServer,_tmpTestDuration,_tmpApLatitude,_tmpApLongitude,_tmpStartTime,_tmpEndTime,_tmpTestCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSession(final long sessionId,
      final Continuation<? super TestSession> continuation) {
    final String _sql = "SELECT * FROM test_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TestSession>() {
      @Override
      public TestSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIperfServer = CursorUtil.getColumnIndexOrThrow(_cursor, "iperfServer");
          final int _cursorIndexOfTestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "testDuration");
          final int _cursorIndexOfApLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "apLatitude");
          final int _cursorIndexOfApLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "apLongitude");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfTestCount = CursorUtil.getColumnIndexOrThrow(_cursor, "testCount");
          final TestSession _result;
          if(_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpIperfServer;
            if (_cursor.isNull(_cursorIndexOfIperfServer)) {
              _tmpIperfServer = null;
            } else {
              _tmpIperfServer = _cursor.getString(_cursorIndexOfIperfServer);
            }
            final int _tmpTestDuration;
            _tmpTestDuration = _cursor.getInt(_cursorIndexOfTestDuration);
            final double _tmpApLatitude;
            _tmpApLatitude = _cursor.getDouble(_cursorIndexOfApLatitude);
            final double _tmpApLongitude;
            _tmpApLongitude = _cursor.getDouble(_cursorIndexOfApLongitude);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpTestCount;
            _tmpTestCount = _cursor.getInt(_cursorIndexOfTestCount);
            _result = new TestSession(_tmpId,_tmpDescription,_tmpIperfServer,_tmpTestDuration,_tmpApLatitude,_tmpApLongitude,_tmpStartTime,_tmpEndTime,_tmpTestCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<TestResult>> getResultsForSession(final long sessionId) {
    final String _sql = "SELECT * FROM test_results WHERE sessionId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"test_results"}, new Callable<List<TestResult>>() {
      @Override
      public List<TestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDistanceFromAp = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceFromAp");
          final int _cursorIndexOfSpeedMbps = CursorUtil.getColumnIndexOrThrow(_cursor, "speedMbps");
          final int _cursorIndexOfTestDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "testDurationSeconds");
          final int _cursorIndexOfDataMB = CursorUtil.getColumnIndexOrThrow(_cursor, "dataMB");
          final int _cursorIndexOfAzimuth = CursorUtil.getColumnIndexOrThrow(_cursor, "azimuth");
          final int _cursorIndexOfPitch = CursorUtil.getColumnIndexOrThrow(_cursor, "pitch");
          final int _cursorIndexOfRoll = CursorUtil.getColumnIndexOrThrow(_cursor, "roll");
          final int _cursorIndexOfAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "accuracy");
          final List<TestResult> _result = new ArrayList<TestResult>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final TestResult _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSessionId;
            _tmpSessionId = _cursor.getLong(_cursorIndexOfSessionId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpDistanceFromAp;
            _tmpDistanceFromAp = _cursor.getDouble(_cursorIndexOfDistanceFromAp);
            final double _tmpSpeedMbps;
            _tmpSpeedMbps = _cursor.getDouble(_cursorIndexOfSpeedMbps);
            final double _tmpTestDurationSeconds;
            _tmpTestDurationSeconds = _cursor.getDouble(_cursorIndexOfTestDurationSeconds);
            final double _tmpDataMB;
            _tmpDataMB = _cursor.getDouble(_cursorIndexOfDataMB);
            final float _tmpAzimuth;
            _tmpAzimuth = _cursor.getFloat(_cursorIndexOfAzimuth);
            final float _tmpPitch;
            _tmpPitch = _cursor.getFloat(_cursorIndexOfPitch);
            final float _tmpRoll;
            _tmpRoll = _cursor.getFloat(_cursorIndexOfRoll);
            final float _tmpAccuracy;
            _tmpAccuracy = _cursor.getFloat(_cursorIndexOfAccuracy);
            _item = new TestResult(_tmpId,_tmpSessionId,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpDistanceFromAp,_tmpSpeedMbps,_tmpTestDurationSeconds,_tmpDataMB,_tmpAzimuth,_tmpPitch,_tmpRoll,_tmpAccuracy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TestResult>> getAllResults() {
    final String _sql = "SELECT * FROM test_results ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"test_results"}, new Callable<List<TestResult>>() {
      @Override
      public List<TestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDistanceFromAp = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceFromAp");
          final int _cursorIndexOfSpeedMbps = CursorUtil.getColumnIndexOrThrow(_cursor, "speedMbps");
          final int _cursorIndexOfTestDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "testDurationSeconds");
          final int _cursorIndexOfDataMB = CursorUtil.getColumnIndexOrThrow(_cursor, "dataMB");
          final int _cursorIndexOfAzimuth = CursorUtil.getColumnIndexOrThrow(_cursor, "azimuth");
          final int _cursorIndexOfPitch = CursorUtil.getColumnIndexOrThrow(_cursor, "pitch");
          final int _cursorIndexOfRoll = CursorUtil.getColumnIndexOrThrow(_cursor, "roll");
          final int _cursorIndexOfAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "accuracy");
          final List<TestResult> _result = new ArrayList<TestResult>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final TestResult _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSessionId;
            _tmpSessionId = _cursor.getLong(_cursorIndexOfSessionId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpDistanceFromAp;
            _tmpDistanceFromAp = _cursor.getDouble(_cursorIndexOfDistanceFromAp);
            final double _tmpSpeedMbps;
            _tmpSpeedMbps = _cursor.getDouble(_cursorIndexOfSpeedMbps);
            final double _tmpTestDurationSeconds;
            _tmpTestDurationSeconds = _cursor.getDouble(_cursorIndexOfTestDurationSeconds);
            final double _tmpDataMB;
            _tmpDataMB = _cursor.getDouble(_cursorIndexOfDataMB);
            final float _tmpAzimuth;
            _tmpAzimuth = _cursor.getFloat(_cursorIndexOfAzimuth);
            final float _tmpPitch;
            _tmpPitch = _cursor.getFloat(_cursorIndexOfPitch);
            final float _tmpRoll;
            _tmpRoll = _cursor.getFloat(_cursorIndexOfRoll);
            final float _tmpAccuracy;
            _tmpAccuracy = _cursor.getFloat(_cursorIndexOfAccuracy);
            _item = new TestResult(_tmpId,_tmpSessionId,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpDistanceFromAp,_tmpSpeedMbps,_tmpTestDurationSeconds,_tmpDataMB,_tmpAzimuth,_tmpPitch,_tmpRoll,_tmpAccuracy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLastResultForSession(final long sessionId,
      final Continuation<? super TestResult> continuation) {
    final String _sql = "SELECT * FROM test_results WHERE sessionId = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TestResult>() {
      @Override
      public TestResult call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfDistanceFromAp = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceFromAp");
          final int _cursorIndexOfSpeedMbps = CursorUtil.getColumnIndexOrThrow(_cursor, "speedMbps");
          final int _cursorIndexOfTestDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "testDurationSeconds");
          final int _cursorIndexOfDataMB = CursorUtil.getColumnIndexOrThrow(_cursor, "dataMB");
          final int _cursorIndexOfAzimuth = CursorUtil.getColumnIndexOrThrow(_cursor, "azimuth");
          final int _cursorIndexOfPitch = CursorUtil.getColumnIndexOrThrow(_cursor, "pitch");
          final int _cursorIndexOfRoll = CursorUtil.getColumnIndexOrThrow(_cursor, "roll");
          final int _cursorIndexOfAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "accuracy");
          final TestResult _result;
          if(_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSessionId;
            _tmpSessionId = _cursor.getLong(_cursorIndexOfSessionId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpDistanceFromAp;
            _tmpDistanceFromAp = _cursor.getDouble(_cursorIndexOfDistanceFromAp);
            final double _tmpSpeedMbps;
            _tmpSpeedMbps = _cursor.getDouble(_cursorIndexOfSpeedMbps);
            final double _tmpTestDurationSeconds;
            _tmpTestDurationSeconds = _cursor.getDouble(_cursorIndexOfTestDurationSeconds);
            final double _tmpDataMB;
            _tmpDataMB = _cursor.getDouble(_cursorIndexOfDataMB);
            final float _tmpAzimuth;
            _tmpAzimuth = _cursor.getFloat(_cursorIndexOfAzimuth);
            final float _tmpPitch;
            _tmpPitch = _cursor.getFloat(_cursorIndexOfPitch);
            final float _tmpRoll;
            _tmpRoll = _cursor.getFloat(_cursorIndexOfRoll);
            final float _tmpAccuracy;
            _tmpAccuracy = _cursor.getFloat(_cursorIndexOfAccuracy);
            _result = new TestResult(_tmpId,_tmpSessionId,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpDistanceFromAp,_tmpSpeedMbps,_tmpTestDurationSeconds,_tmpDataMB,_tmpAzimuth,_tmpPitch,_tmpRoll,_tmpAccuracy);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getTestCountForSession(final long sessionId,
      final Continuation<? super Integer> continuation) {
    final String _sql = "SELECT COUNT(*) FROM test_results WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
