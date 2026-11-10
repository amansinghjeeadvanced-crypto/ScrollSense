package com.example.scrollsense.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.scrollsense.data.local.entities.UsageRecordEntity;
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
public final class UsageRecordDao_Impl implements UsageRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UsageRecordEntity> __insertionAdapterOfUsageRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfIncrementEstimatedVideos;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForDate;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public UsageRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUsageRecordEntity = new EntityInsertionAdapter<UsageRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `usage_records` (`id`,`dateEpochMillis`,`packageName`,`foregroundMinutes`,`estimatedVideos`,`lateNightMinutes`,`longestSessionMinutes`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UsageRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDateEpochMillis());
        statement.bindString(3, entity.getPackageName());
        statement.bindLong(4, entity.getForegroundMinutes());
        statement.bindLong(5, entity.getEstimatedVideos());
        statement.bindLong(6, entity.getLateNightMinutes());
        statement.bindLong(7, entity.getLongestSessionMinutes());
      }
    };
    this.__preparedStmtOfIncrementEstimatedVideos = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE usage_records SET estimatedVideos = estimatedVideos + 1 WHERE dateEpochMillis = ? AND packageName = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteForDate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM usage_records WHERE dateEpochMillis = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM usage_records";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final UsageRecordEntity record,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfUsageRecordEntity.insertAndReturnId(record);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<UsageRecordEntity> records,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUsageRecordEntity.insert(records);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementEstimatedVideos(final String packageName, final long dateEpochMillis,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementEstimatedVideos.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dateEpochMillis);
        _argIndex = 2;
        _stmt.bindString(_argIndex, packageName);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementEstimatedVideos.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteForDate(final long dateEpochMillis,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForDate.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dateEpochMillis);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteForDate.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UsageRecordEntity> getRecord(final String packageName, final long dateEpochMillis) {
    final String _sql = "SELECT * FROM usage_records WHERE dateEpochMillis = ? AND packageName = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dateEpochMillis);
    _argIndex = 2;
    _statement.bindString(_argIndex, packageName);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"usage_records"}, new Callable<UsageRecordEntity>() {
      @Override
      @Nullable
      public UsageRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDateEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochMillis");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfForegroundMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "foregroundMinutes");
          final int _cursorIndexOfEstimatedVideos = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedVideos");
          final int _cursorIndexOfLateNightMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "lateNightMinutes");
          final int _cursorIndexOfLongestSessionMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "longestSessionMinutes");
          final UsageRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDateEpochMillis;
            _tmpDateEpochMillis = _cursor.getLong(_cursorIndexOfDateEpochMillis);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final int _tmpForegroundMinutes;
            _tmpForegroundMinutes = _cursor.getInt(_cursorIndexOfForegroundMinutes);
            final int _tmpEstimatedVideos;
            _tmpEstimatedVideos = _cursor.getInt(_cursorIndexOfEstimatedVideos);
            final int _tmpLateNightMinutes;
            _tmpLateNightMinutes = _cursor.getInt(_cursorIndexOfLateNightMinutes);
            final int _tmpLongestSessionMinutes;
            _tmpLongestSessionMinutes = _cursor.getInt(_cursorIndexOfLongestSessionMinutes);
            _result = new UsageRecordEntity(_tmpId,_tmpDateEpochMillis,_tmpPackageName,_tmpForegroundMinutes,_tmpEstimatedVideos,_tmpLateNightMinutes,_tmpLongestSessionMinutes);
          } else {
            _result = null;
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
  public Object getRecordSync(final String packageName, final long dateEpochMillis,
      final Continuation<? super UsageRecordEntity> $completion) {
    final String _sql = "SELECT * FROM usage_records WHERE dateEpochMillis = ? AND packageName = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dateEpochMillis);
    _argIndex = 2;
    _statement.bindString(_argIndex, packageName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UsageRecordEntity>() {
      @Override
      @Nullable
      public UsageRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDateEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochMillis");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfForegroundMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "foregroundMinutes");
          final int _cursorIndexOfEstimatedVideos = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedVideos");
          final int _cursorIndexOfLateNightMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "lateNightMinutes");
          final int _cursorIndexOfLongestSessionMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "longestSessionMinutes");
          final UsageRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDateEpochMillis;
            _tmpDateEpochMillis = _cursor.getLong(_cursorIndexOfDateEpochMillis);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final int _tmpForegroundMinutes;
            _tmpForegroundMinutes = _cursor.getInt(_cursorIndexOfForegroundMinutes);
            final int _tmpEstimatedVideos;
            _tmpEstimatedVideos = _cursor.getInt(_cursorIndexOfEstimatedVideos);
            final int _tmpLateNightMinutes;
            _tmpLateNightMinutes = _cursor.getInt(_cursorIndexOfLateNightMinutes);
            final int _tmpLongestSessionMinutes;
            _tmpLongestSessionMinutes = _cursor.getInt(_cursorIndexOfLongestSessionMinutes);
            _result = new UsageRecordEntity(_tmpId,_tmpDateEpochMillis,_tmpPackageName,_tmpForegroundMinutes,_tmpEstimatedVideos,_tmpLateNightMinutes,_tmpLongestSessionMinutes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<UsageRecordEntity>> getRecordsSince(final long startEpochMillis) {
    final String _sql = "SELECT * FROM usage_records WHERE dateEpochMillis >= ? ORDER BY dateEpochMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startEpochMillis);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"usage_records"}, new Callable<List<UsageRecordEntity>>() {
      @Override
      @NonNull
      public List<UsageRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDateEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochMillis");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfForegroundMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "foregroundMinutes");
          final int _cursorIndexOfEstimatedVideos = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedVideos");
          final int _cursorIndexOfLateNightMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "lateNightMinutes");
          final int _cursorIndexOfLongestSessionMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "longestSessionMinutes");
          final List<UsageRecordEntity> _result = new ArrayList<UsageRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UsageRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDateEpochMillis;
            _tmpDateEpochMillis = _cursor.getLong(_cursorIndexOfDateEpochMillis);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final int _tmpForegroundMinutes;
            _tmpForegroundMinutes = _cursor.getInt(_cursorIndexOfForegroundMinutes);
            final int _tmpEstimatedVideos;
            _tmpEstimatedVideos = _cursor.getInt(_cursorIndexOfEstimatedVideos);
            final int _tmpLateNightMinutes;
            _tmpLateNightMinutes = _cursor.getInt(_cursorIndexOfLateNightMinutes);
            final int _tmpLongestSessionMinutes;
            _tmpLongestSessionMinutes = _cursor.getInt(_cursorIndexOfLongestSessionMinutes);
            _item = new UsageRecordEntity(_tmpId,_tmpDateEpochMillis,_tmpPackageName,_tmpForegroundMinutes,_tmpEstimatedVideos,_tmpLateNightMinutes,_tmpLongestSessionMinutes);
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
  public Object getRecordsSinceSync(final long startEpochMillis,
      final Continuation<? super List<UsageRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM usage_records WHERE dateEpochMillis >= ? ORDER BY dateEpochMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startEpochMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UsageRecordEntity>>() {
      @Override
      @NonNull
      public List<UsageRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDateEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochMillis");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfForegroundMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "foregroundMinutes");
          final int _cursorIndexOfEstimatedVideos = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedVideos");
          final int _cursorIndexOfLateNightMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "lateNightMinutes");
          final int _cursorIndexOfLongestSessionMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "longestSessionMinutes");
          final List<UsageRecordEntity> _result = new ArrayList<UsageRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UsageRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDateEpochMillis;
            _tmpDateEpochMillis = _cursor.getLong(_cursorIndexOfDateEpochMillis);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final int _tmpForegroundMinutes;
            _tmpForegroundMinutes = _cursor.getInt(_cursorIndexOfForegroundMinutes);
            final int _tmpEstimatedVideos;
            _tmpEstimatedVideos = _cursor.getInt(_cursorIndexOfEstimatedVideos);
            final int _tmpLateNightMinutes;
            _tmpLateNightMinutes = _cursor.getInt(_cursorIndexOfLateNightMinutes);
            final int _tmpLongestSessionMinutes;
            _tmpLongestSessionMinutes = _cursor.getInt(_cursorIndexOfLongestSessionMinutes);
            _item = new UsageRecordEntity(_tmpId,_tmpDateEpochMillis,_tmpPackageName,_tmpForegroundMinutes,_tmpEstimatedVideos,_tmpLateNightMinutes,_tmpLongestSessionMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
