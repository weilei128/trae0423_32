<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">票务销售</h2>
    </div>

    <el-card>
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="选择场次" />
        <el-step title="选择座位" />
        <el-step title="确认订单" />
      </el-steps>
    </el-card>

    <el-card class="mt-20" v-if="currentStep === 0">
      <div slot="header">
        <span>选择影片和场次</span>
      </div>
      <el-select v-model="selectedMovie" placeholder="请选择影片" style="width: 300px; margin-bottom: 20px;" @change="loadSchedules">
        <el-option
          v-for="movie in movieList"
          :key="movie.id"
          :label="movie.title"
          :value="movie.id"
        />
      </el-select>

      <el-table v-if="scheduleList.length > 0" :data="scheduleList" border>
        <el-table-column label="影厅">
          <template slot-scope="scope">
            {{ getScheduleHall(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="开始时间">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="时段" prop="timeSlot">
          <template slot-scope="scope">
            <el-tag>{{ scope.row.timeSlot }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="票价">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="selectSchedule(scope.row)">选座</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="mt-20" v-if="currentStep === 1">
      <div slot="header">
        <span>选择座位</span>
        <el-button size="small" style="float: right;" @click="currentStep = 0">返回选择场次</el-button>
      </div>
      
      <div class="seat-info" v-if="selectedSchedule">
        <p><strong>影片：</strong>{{ selectedScheduleMovie }}</p>
        <p><strong>影厅：</strong>{{ selectedScheduleHall }}</p>
        <p><strong>时间：</strong>{{ formatDateTime(selectedSchedule.startTime) }}</p>
        <p><strong>票价：</strong>¥{{ selectedSchedule.price }}/张</p>
      </div>

      <div class="screen">
        <span>银幕</span>
      </div>

      <div class="seat-container" v-if="seatMap">
        <div v-for="(row, rowIndex) in seatMap" :key="rowIndex" class="seat-row">
          <div class="row-label">{{ rowIndex + 1 }}</div>
          <div
            v-for="(seat, seatIndex) in row"
            :key="seatIndex"
            class="picker-cell"
            :class="{
              available: seat.available,
              sold: !seat.available,
              selected: isSeatSelected(rowIndex + 1, seatIndex + 1),
              empty: seat.row === 0
            }"
            @click="seat.available && toggleSeat(rowIndex + 1, seatIndex + 1)"
          >
            {{ seat.row > 0 ? (seatIndex + 1) : '' }}
          </div>
        </div>
      </div>

      <div class="seat-legend">
        <div class="legend-item">
          <div class="picker-cell available"></div>
          <span>可选</span>
        </div>
        <div class="legend-item">
          <div class="picker-cell sold"></div>
          <span>已售</span>
        </div>
        <div class="legend-item">
          <div class="picker-cell selected"></div>
          <span>已选</span>
        </div>
      </div>

      <div class="selected-seats" v-if="selectedSeats.length > 0">
        <p><strong>已选座位：</strong>{{ selectedSeatsText }}</p>
        <p><strong>总价：</strong>¥{{ totalPrice }}</p>
      </div>

      <el-button type="primary" :disabled="selectedSeats.length === 0" @click="currentStep = 2" style="margin-top: 20px;">
        下一步
      </el-button>
    </el-card>

    <el-card class="mt-20" v-if="currentStep === 2">
      <div slot="header">
        <span>确认订单</span>
        <el-button size="small" style="float: right;" @click="currentStep = 1">返回选座</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="影片">{{ selectedScheduleMovie }}</el-descriptions-item>
        <el-descriptions-item label="影厅">{{ selectedScheduleHall }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ selectedScheduleTime }}</el-descriptions-item>
        <el-descriptions-item label="座位">{{ selectedSeatsText }}</el-descriptions-item>
        <el-descriptions-item label="票价">¥{{ selectedSchedulePrice }}/张</el-descriptions-item>
        <el-descriptions-item label="数量">{{ selectedSeats.length }}张</el-descriptions-item>
        <el-descriptions-item label="总价" span="2"><strong>¥{{ totalPrice }}</strong></el-descriptions-item>
      </el-descriptions>

      <el-form :model="customerForm" :rules="customerRules" ref="customerForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="姓名" prop="customerName">
          <el-input v-model="customerForm.customerName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="customerPhone">
          <el-input v-model="customerForm.customerPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="会员手机号">
          <el-input v-model="memberPhone" placeholder="可选，用于积分" />
          <el-button type="text" @click="checkMember">查询会员</el-button>
        </el-form-item>
        <el-form-item label="使用积分" v-if="currentMember">
          <el-input-number v-model="pointsUsed" :min="0" :max="currentMember.points" :step="100" />
          <span style="margin-left: 10px;">当前积分：{{ currentMember.points }}，100积分=1元</span>
        </el-form-item>
      </el-form>

      <div v-if="pointsUsed > 0" style="margin-bottom: 20px;">
        <p>积分抵扣：¥{{ (pointsUsed / 100).toFixed(2) }}</p>
        <p><strong>实付金额：¥{{ (totalPrice - pointsUsed / 100).toFixed(2) }}</strong></p>
      </div>

      <el-button type="primary" @click="submitOrder">确认下单</el-button>
    </el-card>

    <el-dialog title="订单支付" :visible.sync="paymentDialogVisible" width="500px">
      <div v-if="pendingOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ pendingOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="影片">{{ selectedScheduleMovie }}</el-descriptions-item>
          <el-descriptions-item label="座位">{{ selectedSeatsText }}</el-descriptions-item>
          <el-descriptions-item label="应付金额"><strong style="font-size: 24px; color: #f56c6c;">¥{{ pendingOrder.totalAmount.toFixed(2) }}</strong></el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="payOrder" style="width: 100%;">确认支付</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMovieList } from '@/api/movie'
import { getSchedulesByMovie, getScheduleById } from '@/api/schedule'
import { createOrder, payOrder as payOrderApi } from '@/api/order'
import { getMemberByPhone } from '@/api/member'
import dayjs from 'dayjs'

export default {
  name: 'Tickets',
  data() {
    return {
      currentStep: 0,
      movieList: [],
      scheduleList: [],
      selectedMovie: '',
      selectedSchedule: null,
      seatMap: [],
      selectedSeats: [],
      customerForm: {
        customerName: '',
        customerPhone: ''
      },
      memberPhone: '',
      currentMember: null,
      pointsUsed: 0,
      pendingOrder: null,
      paymentDialogVisible: false,
      customerRules: {
        customerName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        customerPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3456789]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.fetchMovieList()
  },
  computed: {
    selectedSeatsText() {
      return this.selectedSeats.map(s => s.row + '排' + s.seat + '座').join(', ')
    },
    totalPrice() {
      return this.selectedSchedule ? this.selectedSeats.length * this.selectedSchedule.price : 0
    },
    selectedScheduleMovie() {
      if (!this.selectedSchedule) return ''
      return this.selectedSchedule.movie ? this.selectedSchedule.movie.title : ''
    },
    selectedScheduleHall() {
      if (!this.selectedSchedule) return ''
      return this.selectedSchedule.hall ? this.selectedSchedule.hall.name : ''
    },
    selectedScheduleTime() {
      if (!this.selectedSchedule) return ''
      return this.formatDateTime(this.selectedSchedule.startTime)
    },
    selectedSchedulePrice() {
      if (!this.selectedSchedule) return ''
      return this.selectedSchedule.price
    }
  },
  methods: {
    formatDateTime(dt) {
      if (!dt) return ''
      return dayjs(dt).format('YYYY-MM-DD HH:mm')
    },
    getScheduleHall(row) {
      if (!row) return ''
      if (!row.hall) return ''
      return row.hall.name + ' (' + row.hall.type + ')'
    },
    async fetchMovieList() {
      try {
        this.movieList = await getMovieList()
      } catch (error) {
        console.error('获取影片列表失败:', error)
      }
    },
    async loadSchedules(movieId) {
      try {
        this.scheduleList = await getSchedulesByMovie(movieId)
      } catch (error) {
        console.error('获取场次失败:', error)
      }
    },
    async selectSchedule(schedule) {
      this.selectedSchedule = schedule
      await this.loadSeatMap(schedule.id)
      this.currentStep = 1
    },
    async loadSeatMap(scheduleId) {
      try {
        const schedule = await getScheduleById(scheduleId)
        if (schedule && schedule.availableSeats) {
          this.seatMap = JSON.parse(schedule.availableSeats)
        }
        this.selectedSeats = []
      } catch (error) {
        console.error('加载座位图失败:', error)
      }
    },
    isSeatSelected(row, seat) {
      return this.selectedSeats.some(s => s.row === row && s.seat === seat)
    },
    toggleSeat(row, seat) {
      const index = this.selectedSeats.findIndex(s => s.row === row && s.seat === seat)
      if (index > -1) {
        this.selectedSeats.splice(index, 1)
      } else {
        if (this.selectedSeats.length >= 10) {
          this.$message.warning('最多只能选择10个座位')
          return
        }
        this.selectedSeats.push({ row, seat })
      }
    },
    async checkMember() {
      if (!this.memberPhone) {
        this.$message.warning('请输入会员手机号')
        return
      }
      try {
        this.currentMember = await getMemberByPhone(this.memberPhone)
        this.pointsUsed = 0
        this.$message.success('会员信息已加载')
      } catch (error) {
        this.currentMember = null
        this.$message.warning('未找到该会员')
      }
    },
    async submitOrder() {
      try {
        await this.$refs.customerForm.validate(async (valid) => {
          if (valid) {
            const seats = this.selectedSeats.map(s => s.row + '-' + s.seat)
            const orderData = {
              ...this.customerForm,
              scheduleId: this.selectedSchedule.id,
              seats: seats,
              memberId: this.currentMember ? this.currentMember.id : null,
              pointsUsed: this.pointsUsed
            }
            this.pendingOrder = await createOrder(orderData)
            this.paymentDialogVisible = true
          }
        })
      } catch (error) {
        console.error('创建订单失败:', error)
      }
    },
    async payOrder() {
      try {
        await payOrderApi(this.pendingOrder.id)
        this.$message.success('支付成功！')
        this.paymentDialogVisible = false
        this.resetForm()
      } catch (error) {
        console.error('支付失败:', error)
      }
    },
    resetForm() {
      this.currentStep = 0
      this.selectedMovie = ''
      this.scheduleList = []
      this.selectedSchedule = null
      this.seatMap = []
      this.selectedSeats = []
      this.customerForm = { customerName: '', customerPhone: '' }
      this.memberPhone = ''
      this.currentMember = null
      this.pointsUsed = 0
      this.pendingOrder = null
    }
  }
}
</script>

<style lang="scss" scoped>
.mt-20 {
  margin-top: 20px;
}

.seat-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;

  p {
    margin: 5px 0;
  }
}

.screen {
  text-align: center;
  margin: 30px 0 50px 0;

  span {
    display: inline-block;
    width: 60%;
    height: 8px;
    background: linear-gradient(to bottom, #409EFF, #66b1ff);
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.5);
    position: relative;

    &::after {
      content: '银幕';
      position: absolute;
      top: 20px;
      left: 50%;
      transform: translateX(-50%);
      color: #909399;
      font-size: 14px;
    }
  }
}

.seat-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.seat-row {
  display: flex;
  align-items: center;
  margin: 5px 0;
}

.row-label {
  width: 30px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

.seat-legend {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 40px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;

  .picker-cell {
    width: 24px;
    height: 24px;
    line-height: 24px;
    font-size: 10px;
  }
}

.selected-seats {
  margin-top: 20px;
  padding: 15px;
  background-color: #ecf5ff;
  border-radius: 4px;
  border: 1px solid #b3d8ff;

  p {
    margin: 5px 0;
  }
}
</style>
