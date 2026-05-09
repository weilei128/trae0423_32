<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">取票核销</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>核销取票</span>
          </div>
          <el-form :model="verifyForm" label-width="80px">
            <el-form-item label="订单号">
              <el-input v-model="verifyForm.orderNo" placeholder="请输入订单号或扫码" />
            </el-form-item>
            <el-form-item label="核销码">
              <el-input v-model="verifyForm.qrCode" placeholder="请输入二维码内容" />
            </el-form-item>
            <el-button type="primary" @click="verifyOrder" style="width: 100%;">确认核销</el-button>
          </el-form>
        </el-card>

        <el-card class="mt-20">
          <div slot="header">
            <span>查询订单</span>
          </div>
          <el-form label-width="80px">
            <el-form-item label="手机号">
              <el-input v-model="queryPhone" placeholder="请输入手机号查询订单" />
              <el-button type="primary" style="margin-top: 10px;" @click="queryOrders">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table v-if="customerOrders.length > 0" :data="customerOrders" border>
            <el-table-column prop="orderNo" label="订单号" width="200" />
            <el-table-column label="影片">
              <template slot-scope="scope">
                {{ getScheduleMovieTitle(scope.row) }}
              </template>
            </el-table-column>
            <el-table-column label="座位">
              <template slot-scope="scope">
                {{ formatSeats(scope.row.seats) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="核销状态">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isVerified ? 'success' : 'warning'">
                  {{ scope.row.isVerified ? '已核销' : '未核销' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else-if="queryPhone && queried" description="暂无订单" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-if="verifiedOrder">
          <div slot="header">
            <span style="color: #67C23A; font-weight: 600;">
              <i class="el-icon-success"></i> 核销成功
            </span>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ verifiedOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="影片">{{ getOrderMovieTitle(verifiedOrder) }}</el-descriptions-item>
            <el-descriptions-item label="影厅">{{ getOrderHallName(verifiedOrder) }}</el-descriptions-item>
            <el-descriptions-item label="场次">{{ getOrderStartTime(verifiedOrder) }}</el-descriptions-item>
            <el-descriptions-item label="座位">{{ formatSeats(verifiedOrder.seats) }}</el-descriptions-item>
            <el-descriptions-item label="购票人">{{ verifiedOrder.customerName }} ({{ verifiedOrder.customerPhone }})</el-descriptions-item>
            <el-descriptions-item label="核销时间">{{ formatDateTime(verifiedOrder.verifiedAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-else>
          <div slot="header">
            <span>核销说明</span>
          </div>
          <el-timeline>
            <el-timeline-item timestamp="步骤1" placement="top">
              输入订单号或扫描二维码
            </el-timeline-item>
            <el-timeline-item timestamp="步骤2" placement="top">
              点击确认核销按钮
            </el-timeline-item>
            <el-timeline-item timestamp="步骤3" placement="top">
              系统验证订单有效性
            </el-timeline-item>
            <el-timeline-item timestamp="步骤4" placement="top">
              核销成功，打印电影票
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { verifyOrder, getOrdersByPhone } from '@/api/order'
import dayjs from 'dayjs'

export default {
  name: 'Verify',
  data() {
    return {
      verifyForm: {
        orderNo: '',
        qrCode: ''
      },
      queryPhone: '',
      queried: false,
      customerOrders: [],
      verifiedOrder: null
    }
  },
  methods: {
    formatDateTime(dt) {
      if (!dt) return ''
      return dayjs(dt).format('YYYY-MM-DD HH:mm')
    },
    getScheduleMovieTitle(row) {
      if (!row || !row.schedule) return ''
      return row.schedule.movie ? row.schedule.movie.title : ''
    },
    getOrderMovieTitle(order) {
      if (!order || !order.schedule) return ''
      return order.schedule.movie ? order.schedule.movie.title : ''
    },
    getOrderHallName(order) {
      if (!order || !order.schedule) return ''
      return order.schedule.hall ? order.schedule.hall.name : ''
    },
    getOrderStartTime(order) {
      if (!order || !order.schedule) return ''
      return this.formatDateTime(order.schedule.startTime)
    },
    formatSeats(seatsJson) {
      try {
        const seats = JSON.parse(seatsJson)
        return seats.map(s => {
          const [row, seat] = s.split('-')
          return row + '排' + seat + '座'
        }).join(', ')
      } catch {
        return seatsJson
      }
    },
    getStatusType(status) {
      if (status === '已支付') return 'success'
      if (status === '待支付') return 'warning'
      return 'info'
    },
    async verifyOrder() {
      if (!this.verifyForm.orderNo && !this.verifyForm.qrCode) {
        this.$message.warning('请输入订单号或核销码')
        return
      }

      try {
        const verifyCode = this.verifyForm.qrCode || this.verifyForm.orderNo
        this.verifiedOrder = await verifyOrder(verifyCode)
        this.$message.success('核销成功！')
      } catch (error) {
        console.error('核销失败:', error)
      }
    },
    async queryOrders() {
      if (!this.queryPhone) {
        this.$message.warning('请输入手机号')
        return
      }

      try {
        this.queried = true
        this.customerOrders = await getOrdersByPhone(this.queryPhone)
      } catch (error) {
        console.error('查询订单失败:', error)
        this.customerOrders = []
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.mt-20 {
  margin-top: 20px;
}
</style>
