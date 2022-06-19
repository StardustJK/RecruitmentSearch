## 版本
v1.1  
- 支持多条件筛选、排序
- 搜索框可查询职位名称、职位关键字、职位类型、城市（精确匹配）、地区（模糊搜索）、企业名称
- 返回结果中添加总数:total字段
- 增加薪资上限筛选（3k以下）

### 环境信息
1. es版本：7.9.3
2. jdk：11
3. 需要连接校园网，以访问es服务器

## 开发人员
1. 访问localhost:8080/hello查看项目是否配置成功 
2. 部署后访问后端服务器**【见内部开发文档】**
3. 执行RecruitmentsearchApplicationTests中的test()返回”58*“前缀下的所有索引，需要连接校园网。 
4. es操作参考https://blog.csdn.net/qq_41203483/article/details/121560366