<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">影片管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增影片</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索影片名称"
        prefix-icon="el-icon-search"
        style="width: 300px;"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
        clearable
      />
      <el-button type="primary" icon="el-icon-search" style="margin-left: 10px;" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="movieList" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="影片名称" />
      <el-table-column prop="genre" label="类型" width="120" />
      <el-table-column label="时长">
        <template slot-scope="scope">
          {{ scope.row.duration }} 分钟
        </template>
      </el-table-column>
      <el-table-column prop="rating" label="分级" width="80" />
      <el-table-column prop="releaseDate" label="上映日期" width="120" />
      <el-table-column prop="director" label="导演" />
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'info'">
            {{ scope.row.isActive ? '上映中' : '已下架' }}
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
        <el-form-item label="影片名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入影片名称" />
        </el-form-item>
        <el-form-item label="类型" prop="genre">
          <el-select v-model="form.genre" placeholder="请选择影片类型" style="width: 100%;">
            <el-option label="动作" value="动作" />
            <el-option label="喜剧" value="喜剧" />
            <el-option label="爱情" value="爱情" />
            <el-option label="科幻" value="科幻" />
            <el-option label="动画" value="动画" />
            <el-option label="惊悚" value="惊悚" />
            <el-option label="剧情" value="剧情" />
            <el-option label="战争" value="战争" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="300" />
        </el-form-item>
        <el-form-item label="分级" prop="rating">
          <el-select v-model="form.rating" placeholder="请选择分级" style="width: 100%;">
            <el-option label="G" value="G" />
            <el-option label="PG" value="PG" />
            <el-option label="PG-13" value="PG-13" />
            <el-option label="R" value="R" />
            <el-option label="NC-17" value="NC-17" />
          </el-select>
        </el-form-item>
        <el-form-item label="上映日期" prop="releaseDate">
          <el-date-picker
            v-model="form.releaseDate"
            type="date"
            placeholder="选择上映日期"
            style="width: 100%;"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item label="导演" prop="director">
          <el-input v-model="form.director" placeholder="请输入导演名称" />
        </el-form-item>
        <el-form-item label="主演">
          <el-input v-model="form.actors" placeholder="请输入主演，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="海报">
          <el-input v-model="form.poster" placeholder="请输入海报URL" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            type="textarea"
            v-model="form.description"
            :rows="3"
            placeholder="请输入影片简介"
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
import { getMovieList, searchMovies, createMovie, updateMovie, deleteMovie } from '@/api/movie'

export default {
  name: 'Movies',
  data() {
    return {
      searchKeyword: '',
      movieList: [],
      dialogVisible: false,
      isEdit: false,
      form: {
        id: null,
        title: '',
        genre: '',
        duration: 90,
        rating: 'PG',
        releaseDate: '',
        director: '',
        actors: '',
        poster: '',
        description: '',
        isActive: true
      },
      rules: {
        title: [{ required: true, message: '请输入影片名称', trigger: 'blur' }],
        genre: [{ required: true, message: '请选择影片类型', trigger: 'change' }],
        duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
        rating: [{ required: true, message: '请选择分级', trigger: 'change' }],
        releaseDate: [{ required: true, message: '请选择上映日期', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.fetchMovieList()
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑影片' : '新增影片'
    }
  },
  methods: {
    async fetchMovieList() {
      try {
        this.movieList = await getMovieList()
      } catch (error) {
        console.error('获取影片列表失败:', error)
      }
    },
    async handleSearch() {
      try {
        if (this.searchKeyword) {
          this.movieList = await searchMovies(this.searchKeyword)
        } else {
          this.fetchMovieList()
        }
      } catch (error) {
        console.error('搜索失败:', error)
      }
    },
    handleAdd() {
      this.isEdit = false
      this.form = {
        id: null,
        title: '',
        genre: '',
        duration: 90,
        rating: 'PG',
        releaseDate: '',
        director: '',
        actors: '',
        poster: '',
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
            if (this.isEdit) {
              await updateMovie(this.form.id, this.form)
              this.$message.success('修改成功')
            } else {
              await createMovie(this.form)
              this.$message.success('添加成功')
            }
            this.dialogVisible = false
            this.fetchMovieList()
          }
        })
      } catch (error) {
        console.error('保存失败:', error)
      }
    },
    handleDelete(row) {
      this.$confirm('确定要删除该影片吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteMovie(row.id)
          this.$message.success('删除成功')
          this.fetchMovieList()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    }
  }
}
</script>
