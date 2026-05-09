<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">排片管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增排片</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="filterHall" placeholder="按影厅筛选" style="width: 200px; margin-right: 10px;" clearable @change="fetchScheduleList">
        <el-option
          v-for="hall in hallList"
          :key="hall.id"
          :label="hall.name"
          :value="hall.id"
        />
      </el-select>
      <el-select v-model="filterMovie" placeholder="按影片筛选" style="width: 200px;" clearable @change="fetchScheduleList">
        <el-option
          v-for="movie in movieList"
          :key="movie.id"
          :label="movie.title"
          :value="movie.id"
        />
      </el-select>
    </div>

    <el-table :data="scheduleList" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="影片">
        <template slot-scope="scope">
          {{ getMovieTitle(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="影厅">
        <template slot-scope="scope">
          {{ getHallInfo(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column prop="timeSlot" label="时段" width="80">
        <template slot-scope="scope">
          <el-tag>{{ scope.row.timeSlot }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column label="结束时间">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="票价">
        <template slot-scope="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'info'">
            {{ scope.row.isActive ? '正常' : '已取消' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增排片" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="选择影片" prop="movieId">
          <el-select v-model="form.movieId" placeholder="请选择影片" style="width: 100%;" @change="onMovieChange">
            <el-option
              v-for="movie in movieList"
              :key="movie.id"
              :label="movie.title + ' (' + movie.duration + '分钟)'"
              :value="movie.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择影厅" prop="hallId">
          <el-select v-model="form.hallId" placeholder="请选择影厅" style="width: 100%;" @change="onHallChange">
            <el-option
              v-for="hall in hallList"
              :key="hall.id"
              :label="hall.name + ' (' + hall.type + ')'"
              :value="hall.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%;"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd'T'HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number v-model="form.price" :min="0" :step="1" />
          <span style="margin-left: 10px; color: #909399;">（留空将自动计算）</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getScheduleList, getSchedulesByMovie, getSchedulesByHall, createSchedule, deleteSchedule } from '@/api/schedule'
import { getMovieList } from '@/api/movie'
import { getHallList } from '@/api/hall'
import dayjs from 'dayjs'

export default {
  name: 'Schedules',
  data() {
    return {
      scheduleList: [],
      movieList: [],
      hallList: [],
      filterHall: '',
      filterMovie: '',
      dialogVisible: false,
      form: {
        movieId: '',
        hallId: '',
        startTime: '',
        price: null
      },
      selectedMovie: null,
      selectedHall: null,
      rules: {
        movieId: [{ required: true, message: '请选择影片', trigger: 'change' }],
        hallId: [{ required: true, message: '请选择影厅', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.fetchMovieList()
    this.fetchHallList()
    this.fetchScheduleList()
  },
  methods: {
    formatDateTime(dt) {
      if (!dt) return ''
      return dayjs(dt).format('YYYY-MM-DD HH:mm')
    },
    getMovieTitle(row) {
      return row.movie ? row.movie.title : ''
    },
    getHallInfo(row) {
      return row.hall ? row.hall.name + ' (' + row.hall.type + ')' : ''
    },
    async fetchMovieList() {
      try {
        this.movieList = await getMovieList()
      } catch (error) {
        console.error('获取影片列表失败:', error)
      }
    },
    async fetchHallList() {
      try {
        this.hallList = await getHallList()
      } catch (error) {
        console.error('获取影厅列表失败:', error)
      }
    },
    async fetchScheduleList() {
      try {
        if (this.filterMovie) {
          this.scheduleList = await getSchedulesByMovie(this.filterMovie)
        } else if (this.filterHall) {
          this.scheduleList = await getSchedulesByHall(this.filterHall)
        } else {
          this.scheduleList = await getScheduleList()
        }
      } catch (error) {
        console.error('获取排片列表失败:', error)
      }
    },
    onMovieChange(movieId) {
      this.selectedMovie = this.movieList.find(m => m.id === movieId)
    },
    onHallChange(hallId) {
      this.selectedHall = this.hallList.find(h => h.id === hallId)
    },
    handleAdd() {
      this.form = {
        movieId: '',
        hallId: '',
        startTime: '',
        price: null
      }
      this.dialogVisible = true
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate(async (valid) => {
          if (valid) {
            const data = {
              movieId: this.form.movieId,
              hallId: this.form.hallId,
              startTime: this.form.startTime
            }
            if (this.form.price) {
              data.price = this.form.price
            }
            await createSchedule(data)
            this.$message.success('排片成功')
            this.dialogVisible = false
            this.fetchScheduleList()
          }
        })
      } catch (error) {
        console.error('排片失败:', error)
      }
    },
    handleDelete(row) {
      this.$confirm('确定要取消该排片吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteSchedule(row.id)
          this.$message.success('取消成功')
          this.fetchScheduleList()
        } catch (error) {
          console.error('取消排片失败:', error)
        }
      }).catch(() => {})
    }
  }
}
</script>
