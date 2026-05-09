<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">影厅管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增影厅</el-button>
    </div>

    <el-table :data="hallList" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="影厅名称" />
      <el-table-column prop="type" label="类型" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.type === 'IMAX'" type="danger">IMAX</el-tag>
          <el-tag v-else-if="scope.row.type === '杜比'" type="warning">杜比</el-tag>
          <el-tag v-else type="info">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalSeats" label="总座位数" width="100" />
      <el-table-column label="座位布局">
        <template slot-scope="scope">
          {{ scope.row.rowsCount }}行 x {{ scope.row.seatsPerRow }}座
        </template>
      </el-table-column>
      <el-table-column label="基础票价">
        <template slot-scope="scope">
          ¥{{ scope.row.basePrice }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'info'">
            {{ scope.row.isActive ? '可用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="影厅名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入影厅名称" />
        </el-form-item>
        <el-form-item label="影厅类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择影厅类型" style="width: 100%;">
            <el-option label="普通影厅" value="普通" />
            <el-option label="IMAX影厅" value="IMAX" />
            <el-option label="杜比影厅" value="杜比" />
          </el-select>
        </el-form-item>
        <el-form-item label="行数" prop="rowsCount">
          <el-input-number v-model="form.rowsCount" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="每排座位数" prop="seatsPerRow">
          <el-input-number v-model="form.seatsPerRow" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="基础票价" prop="basePrice">
          <el-input-number v-model="form.basePrice" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            type="textarea"
            v-model="form.description"
            :rows="3"
            placeholder="请输入影厅描述"
          />
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
import { getHallList, createHall, updateHall, deleteHall } from '@/api/hall'

export default {
  name: 'Halls',
  data() {
    return {
      hallList: [],
      dialogVisible: false,
      isEdit: false,
      form: {
        id: null,
        name: '',
        type: '普通',
        rowsCount: 10,
        seatsPerRow: 15,
        basePrice: 30,
        description: '',
        isActive: true
      },
      rules: {
        name: [{ required: true, message: '请输入影厅名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择影厅类型', trigger: 'change' }],
        rowsCount: [{ required: true, message: '请输入行数', trigger: 'blur' }],
        seatsPerRow: [{ required: true, message: '请输入每排座位数', trigger: 'blur' }],
        basePrice: [{ required: true, message: '请输入基础票价', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.fetchHallList()
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑影厅' : '新增影厅'
    }
  },
  methods: {
    async fetchHallList() {
      try {
        this.hallList = await getHallList()
      } catch (error) {
        console.error('获取影厅列表失败:', error)
      }
    },
    handleAdd() {
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        type: '普通',
        rowsCount: 10,
        seatsPerRow: 15,
        basePrice: 30,
        description: '',
        isActive: true
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate(async (valid) => {
          if (valid) {
            this.form.totalSeats = this.form.rowsCount * this.form.seatsPerRow
            if (this.isEdit) {
              await updateHall(this.form.id, this.form)
              this.$message.success('修改成功')
            } else {
              await createHall(this.form)
              this.$message.success('添加成功')
            }
            this.dialogVisible = false
            this.fetchHallList()
          }
        })
      } catch (error) {
        console.error('保存失败:', error)
      }
    },
    handleDelete(row) {
      this.$confirm('确定要删除该影厅吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteHall(row.id)
          this.$message.success('删除成功')
          this.fetchHallList()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    }
  }
}
</script>
