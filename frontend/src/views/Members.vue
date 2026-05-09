<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">会员管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增会员</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名或手机号"
        prefix-icon="el-icon-search"
        style="width: 300px;"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
        clearable
      />
      <el-button type="primary" icon="el-icon-search" style="margin-left: 10px;" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="memberList" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="points" label="积分" width="100">
        <template slot-scope="scope">
          <el-tag type="success">{{ scope.row.points }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="会员等级" width="120">
        <template slot-scope="scope">
          <el-tag :type="getLevelType(scope.row.level)">{{ scope.row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'info'">
            {{ scope.row.isActive ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template slot-scope="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" @click="viewPoints(scope.row)">积分记录</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱（可选）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="积分记录" :visible.sync="pointsDialogVisible" width="600px">
      <el-table :data="pointsHistory" border>
        <el-table-column prop="type" label="类型">
          <template slot-scope="scope">
            <el-tag :type="scope.row.points > 0 ? 'success' : 'danger'">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.points > 0 ? '#67C23A' : '#F56C6C' }">
              {{ scope.row.points > 0 ? '+' : '' }}{{ scope.row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="时间">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getMemberList, searchMembers, createMember, updateMember, deleteMember, getPointsHistory } from '@/api/member'
import dayjs from 'dayjs'

export default {
  name: 'Members',
  data() {
    return {
      searchKeyword: '',
      memberList: [],
      dialogVisible: false,
      pointsDialogVisible: false,
      isEdit: false,
      form: {
        id: null,
        name: '',
        phone: '',
        email: '',
        isActive: true
      },
      pointsHistory: [],
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3456789]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.fetchMemberList()
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑会员' : '新增会员'
    }
  },
  methods: {
    formatDateTime(dt) {
      if (!dt) return ''
      return dayjs(dt).format('YYYY-MM-DD HH:mm')
    },
    getLevelType(level) {
      if (level === '钻石会员') return 'danger'
      if (level === '黄金会员') return 'warning'
      if (level === '白银会员') return 'info'
      return 'success'
    },
    async fetchMemberList() {
      try {
        this.memberList = await getMemberList()
      } catch (error) {
        console.error('获取会员列表失败:', error)
      }
    },
    async handleSearch() {
      try {
        if (this.searchKeyword) {
          this.memberList = await searchMembers(this.searchKeyword)
        } else {
          this.fetchMemberList()
        }
      } catch (error) {
        console.error('搜索失败:', error)
      }
    },
    handleAdd() {
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        phone: '',
        email: '',
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
            if (this.isEdit) {
              await updateMember(this.form.id, this.form)
              this.$message.success('修改成功')
            } else {
              await createMember(this.form)
              this.$message.success('添加成功')
            }
            this.dialogVisible = false
            this.fetchMemberList()
          }
        })
      } catch (error) {
        console.error('保存失败:', error)
      }
    },
    handleDelete(row) {
      this.$confirm('确定要删除该会员吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteMember(row.id)
          this.$message.success('删除成功')
          this.fetchMemberList()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    },
    async viewPoints(member) {
      try {
        this.pointsHistory = await getPointsHistory(member.id)
        this.pointsDialogVisible = true
      } catch (error) {
        console.error('获取积分记录失败:', error)
      }
    }
  }
}
</script>
