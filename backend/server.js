/**
 * MODULE 5: Node.js/Express & MongoDB Sync Route
 * Backend service handling heterogeneous fitness and mood logs synced from Android clients.
 */

const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/rhythmfit';

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// --- Mongoose Schemas & Models ---

// Heterogeneous Activity Log Schema
const ActivityLogSchema = new mongoose.Schema({
  deviceId: { type: String, required: true, index: true },
  localId: { type: Number, required: true },
  activityName: { type: String, required: true },
  durationMins: { type: Number, required: true },
  estimatedKcal: { type: Number, default: 0 },
  metValue: { type: Number, default: 3.0 },
  steps: { type: Number, default: 0 },
  isMicroBreak: { type: Boolean, default: false },
  tags: [{ type: String }],
  timestamp: { type: Number, required: true, index: true },
  syncedAt: { type: Date, default: Date.now },
  // Flexible unstructured payload field for custom client metrics
  metadata: { type: mongoose.Schema.Types.Mixed, default: {} }
}, { timestamps: true });

// Compound index to guarantee idempotency across duplicate client sync retries
ActivityLogSchema.index({ deviceId: 1, localId: 1, timestamp: 1 }, { unique: true });

// Mood Entry Schema
const MoodEntrySchema = new mongoose.Schema({
  deviceId: { type: String, required: true, index: true },
  localId: { type: Number, required: true },
  moodLevel: { type: Number, required: true, min: 1, max: 5 },
  moodName: { type: String, required: true },
  note: { type: String, default: '' },
  timestamp: { type: Number, required: true },
  syncedAt: { type: Date, default: Date.now }
}, { timestamps: true });

MoodEntrySchema.index({ deviceId: 1, localId: 1, timestamp: 1 }, { unique: true });

const ActivityLog = mongoose.model('ActivityLog', ActivityLogSchema);
const MoodEntry = mongoose.model('MoodEntry', MoodEntrySchema);

// --- Healthcheck Route ---
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
});

// --- MODULE 5 Core Sync Route ---
/**
 * POST /api/sync/logs
 * Accepts heterogeneous array of activities and moods from Room Database.
 * Employs bulk upsert operations with idempotency keys.
 */
app.post('/api/sync/logs', async (req, res) => {
  try {
    const { deviceId, activities = [], moods = [] } = req.body;

    if (!deviceId) {
      return res.status(400).json({
        success: false,
        error: 'Missing required deviceId header or body field'
      });
    }

    const activityOps = activities.map(act => ({
      updateOne: {
        filter: { deviceId, localId: act.localId, timestamp: act.timestamp },
        update: {
          $set: {
            deviceId,
            localId: act.localId,
            activityName: act.activityName,
            durationMins: act.durationMins,
            estimatedKcal: act.estimatedKcal,
            metValue: act.metValue,
            steps: act.steps || 0,
            isMicroBreak: Boolean(act.isMicroBreak),
            tags: act.tags || [],
            timestamp: act.timestamp,
            metadata: act.metadata || {},
            syncedAt: new Date()
          }
        },
        upsert: true
      }
    }));

    const moodOps = moods.map(mood => ({
      updateOne: {
        filter: { deviceId, localId: mood.localId, timestamp: mood.timestamp },
        update: {
          $set: {
            deviceId,
            localId: mood.localId,
            moodLevel: mood.moodLevel,
            moodName: mood.moodName,
            note: mood.note || '',
            timestamp: mood.timestamp,
            syncedAt: new Date()
          }
        },
        upsert: true
      }
    }));

    let activitiesSynced = 0;
    let moodsSynced = 0;

    if (activityOps.length > 0) {
      const actResult = await ActivityLog.bulkWrite(activityOps, { ordered: false });
      activitiesSynced = (actResult.upsertedCount || 0) + (actResult.modifiedCount || 0);
    }

    if (moodOps.length > 0) {
      const moodResult = await MoodEntry.bulkWrite(moodOps, { ordered: false });
      moodsSynced = (moodResult.upsertedCount || 0) + (moodResult.modifiedCount || 0);
    }

    return res.status(200).json({
      success: true,
      message: 'Batch logs synced successfully',
      syncedAt: new Date().toISOString(),
      counts: {
        activitiesProcessed: activities.length,
        activitiesSynced,
        moodsProcessed: moods.length,
        moodsSynced
      }
    });
  } catch (error) {
    console.error('Error during log sync:', error);
    return res.status(500).json({
      success: false,
      error: 'Failed to process sync payload',
      details: error.message
    });
  }
});

// --- GET endpoint for querying user synced timeline ---
app.get('/api/logs/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const limit = parseInt(req.query.limit) || 30;

    const activities = await ActivityLog.find({ deviceId })
      .sort({ timestamp: -1 })
      .limit(limit)
      .lean();

    const moods = await MoodEntry.find({ deviceId })
      .sort({ timestamp: -1 })
      .limit(limit)
      .lean();

    res.status(200).json({
      success: true,
      deviceId,
      data: {
        activities,
        moods
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Start Server (with graceful MongoDB connection)
if (process.env.NODE_ENV !== 'test') {
  mongoose.connect(MONGODB_URI)
    .then(() => {
      console.log(`Connected to MongoDB: ${MONGODB_URI}`);
      app.listen(PORT, () => {
        console.log(`RhythmFit Sync Backend listening on port ${PORT}`);
      });
    })
    .catch((err) => {
      console.warn(`MongoDB connection skipped (offline/standalone mode): ${err.message}`);
      app.listen(PORT, () => {
        console.log(`RhythmFit Sync Backend running in standalone mode on port ${PORT}`);
      });
    });
}

module.exports = app;
