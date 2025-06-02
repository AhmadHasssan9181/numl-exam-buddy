package com.noobdev.numlexambuddy.data.dao

import androidx.room.RoomWarnings

/**
 * Common Room annotations to fix signature issues in DAO interfaces
 */
@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH, RoomWarnings.MISSING_JAVA_TMP_DIR)
annotation class RoomCompat
