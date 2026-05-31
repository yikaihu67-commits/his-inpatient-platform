<script>
export default {
  onLaunch() {}
}
</script>

<style>
page {
  background: #f3f6fb;
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx;
}

.card {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 22rpx rgba(15, 23, 42, 0.05);
}

.section-title {
  display: block;
  margin-bottom: 16rpx;
  color: #0f172a;
  font-size: 32rpx;
  font-weight: 700;
}

.muted {
  color: #64748b;
  font-size: 26rpx;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.btn {
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 14rpx;
  background: #2563eb;
  color: #fff;
  font-size: 30rpx;
}

.btn.ghost {
  background: #eef4ff;
  color: #2563eb;
}

.btn.danger {
  background: #fee2e2;
  color: #b91c1c;
}

.tag {
  display: inline-block;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2563eb;
  font-size: 24rpx;
  white-space: nowrap;
}

.tag.success {
  background: #dcfce7;
  color: #15803d;
}

.tag.warning {
  background: #fef3c7;
  color: #b45309;
}

.tag.danger {
  background: #fee2e2;
  color: #b91c1c;
}

.tag.info {
  background: #e2e8f0;
  color: #475569;
}

.field {
  margin-bottom: 18rpx;
}

.label {
  display: block;
  margin-bottom: 8rpx;
  color: #64748b;
  font-size: 26rpx;
}

.input {
  height: 84rpx;
  box-sizing: border-box;
  padding: 0 20rpx;
  border: 2rpx solid #dbe4ef;
  border-radius: 14rpx;
  background: #fff;
  font-size: 30rpx;
}

.empty {
  padding: 46rpx 0;
  color: #94a3b8;
  text-align: center;
  font-size: 28rpx;
}
</style>
