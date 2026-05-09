<template>
  <div class="dashboard">
    <div class="page-header">
      <h2 class="page-title">营业报表</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon blue">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日营业额</div>
            <div class="stat-value">¥{{ formatNumber(dailyReport.totalRevenue) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon green">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日售票数</div>
            <div class="stat-value">{{ dailyReport.totalTickets || 0 }} 张</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orange">
            <i class="el-icon-pie-chart"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日上座率</div>
            <div class="stat-value">{{ formatRate(dailyReport.occupancyRate) }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon purple">
            <i class="el-icon-date"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">本周营业额</div>
            <div class="stat-value">¥{{ formatNumber(weeklyReport.totalWeeklyRevenue) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-20">
      <div slot="header">
        <span>今日影片票房排行</span>
      </div>
      <el-table :data="dailyReport.movieStats || []" border>
        <el-table-column label="排名" type="index" width="80" align="center">
          <template slot-scope="scope">
            <span v-if="scope.$index < 3" class="rank-badge" :class="'rank-' + (scope.$index + 1)">
              {{ scope.$index + 1 }}
            </span>
            <span v-else>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="movieName" label="影片名称" />
        <el-table-column prop="tickets" label="售票数" align="center" />
        <el-table-column label="票房">
          <template slot-scope="scope">
            ¥{{ formatNumber(scope.row.revenue) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="mt-20">
      <div slot="header">
        <span>近7天营业额趋势</span>
      </div>
      <div class="trend-chart">
        <el-table :data="weeklyReport.dailyReports || []" border>
          <el-table-column prop="date" label="日期" width="150" />
          <el-table-column label="营业额">
            <template slot-scope="scope">
              ¥{{ formatNumber(scope.row.totalRevenue) }}
            </template>
          </el-table-column>
          <el-table-column label="售票数">
            <template slot-scope="scope">
              {{ scope.row.totalTickets }} 张
            </template>
          </el-table-column>
          <el-table-column label="上座率">
            <template slot-scope="scope">
              {{ formatRate(scope.row.occupancyRate) }}%
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getDailyReport, getWeeklyReport } from '@/api/report'

export default {
  name: 'Dashboard',
  data() {
    return {
      dailyReport: {},
      weeklyReport: {}
    }
  },
  mounted() {
    this.fetchDailyReport()
    this.fetchWeeklyReport()
  },
  methods: {
    formatNumber(val) {
      if (val == null || isNaN(val)) return '0.00'
      return val.toFixed(2)
    },
    formatRate(val) {
      if (val == null || isNaN(val)) return '0'
      return val.toFixed(1)
    },
    async fetchDailyReport() {
      try {
        this.dailyReport = await getDailyReport()
      } catch (error) {
        console.error('获取日报表失败:', error)
      }
    },
    async fetchWeeklyReport() {
      try {
        this.weeklyReport = await getWeeklyReport()
      } catch (error) {
        console.error('获取周报表失败:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 10px;
}

.stat-card {
  .el-card__body {
    display: flex;
    align-items: center;
    padding: 20px;
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;

  i {
    font-size: 30px;
    color: #fff;
  }

  &.blue {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.green {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  }

  &.orange {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.purple {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
}

.stat-info {
  .stat-label {
    color: #909399;
    font-size: 14px;
    margin-bottom: 5px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}

.mt-20 {
  margin-top: 20px;
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  color: #fff;
  font-weight: 600;

  &.rank-1 {
    background-color: #ffd700;
  }

  &.rank-2 {
    background-color: #c0c0c0;
  }

  &.rank-3 {
    background-color: #cd7f32;
  }
}
</style>
