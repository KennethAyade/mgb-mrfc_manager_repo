/**
 * ================================================
 * NOTIFICATIONS CONTROLLER
 * ================================================
 * Handles user notifications management
 */

import { Request, Response } from 'express';
import { Notification } from '../models';

/**
 * List user notifications
 * GET /api/v1/notifications
 */
export const listNotifications = async (req: Request, res: Response): Promise<void> => {
  try {
    const { is_read, type, page = '1', limit = '20' } = req.query;
    const currentUser = (req as any).user;

    // Build filter conditions (always filter by current user)
    const where: any = { user_id: currentUser?.userId };
    if (is_read !== undefined) where.is_read = is_read === 'true';
    if (type) where.type = type;

    // Query notifications with pagination
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    const { count, rows: notifications } = await Notification.findAndCountAll({
      where,
      limit: limitNum,
      offset,
      order: [['created_at', 'DESC']]
    });

    res.json({
      success: true,
      data: {
        notifications,
        pagination: {
          page: pageNum,
          limit: limitNum,
          total: count,
          totalPages: Math.ceil(count / limitNum),
          hasNext: pageNum * limitNum < count,
          hasPrev: pageNum > 1
        }
      }
    });
  } catch (error: any) {
    console.error('Notification listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'NOTIFICATION_LISTING_FAILED',
        message: error.message || 'Failed to retrieve notifications'
      }
    });
  }
};

/**
 * Count unread notifications
 * GET /api/v1/notifications/count-unread
 */
export const countUnread = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;

    const count = await Notification.count({
      where: {
        user_id: currentUser?.userId,
        is_read: false
      }
    });

    res.json({
      success: true,
      data: {
        unread_count: count
      }
    });
  } catch (error: any) {
    console.error('Count unread notifications error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'COUNT_UNREAD_FAILED',
        message: error.message || 'Failed to count unread notifications'
      }
    });
  }
};

/**
 * Mark notification as read
 * PUT /api/v1/notifications/:id/read
 */
export const markAsRead = async (req: Request, res: Response): Promise<void> => {
  try {
    const notificationId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find notification (ensure it belongs to current user)
    const notification = await Notification.findOne({
      where: { id: notificationId, user_id: currentUser?.userId }
    });

    if (!notification) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOTIFICATION_NOT_FOUND',
          message: 'Notification not found'
        }
      });
      return;
    }

    // Mark as read
    await notification.update({ is_read: true, read_at: new Date() });

    res.json({
      success: true,
      message: 'Notification marked as read',
      data: notification
    });
  } catch (error: any) {
    console.error('Mark notification as read error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'MARK_READ_FAILED',
        message: error.message || 'Failed to mark notification as read'
      }
    });
  }
};

/**
 * Delete notification
 * DELETE /api/v1/notifications/:id
 */
export const deleteNotification = async (req: Request, res: Response): Promise<void> => {
  try {
    const notificationId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find notification (ensure it belongs to current user)
    const notification = await Notification.findOne({
      where: { id: notificationId, user_id: currentUser?.userId }
    });

    if (!notification) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOTIFICATION_NOT_FOUND',
          message: 'Notification not found'
        }
      });
      return;
    }

    // Delete notification
    await notification.destroy();

    res.json({
      success: true,
      message: 'Notification deleted successfully'
    });
  } catch (error: any) {
    console.error('Notification deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'NOTIFICATION_DELETION_FAILED',
        message: error.message || 'Failed to delete notification'
      }
    });
  }
};
