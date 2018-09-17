&emsp;&emsp;一直都想尝试做前后端分离，我之前一直是学 Java 的，所以后端选择了 Spring Boot；前端选择了 Vue.js 这个轻量、易上手的框架。网上其实已经有了不少 Spring Boot 和 Vue.js 整合的资料，Github 上就有好多 repo，但是每当我指望按图索骥的时候就会出现各种各样奇怪的 bug，上 Stack Overflow 问了也没人搭理。前前后后研究了差不多三个星期，现在总算是理清楚了。<br>
&emsp;&emsp;本文重点介绍我在实践过程中的基本流程，以及我遇到的一个困扰了我好久的问题，就是如何 CORS。<br>
# 框架版本
- Spring Boot: 2.0.4.RELEASE（JDK 是1.8）
- Vue.js: 2.x
# 基本流程
## 前端：编写 Vue 组件
&emsp;&emsp;首先用 vue-cli 搭好脚手架，我这个 Demo 用到的第三方库有：
 - axios：负责 HTTP 请求
 - bootstrap-vue：Bootstrap 和 Vue.js 的整合，方便设计页面
 - vue-router：管理路由
 - qs：实现 CORS

然后写一个登录组件：
```html
<!-- 下面是我直接从 bootstrap-vue 文档抄下来的模板  -->
<template>
  <div>
    <b-form @submit="onSubmit" @reset="onReset" v-if="show">
      <b-form-group id="exampleInputGroup1"
                    label="Username:"
                    label-for="exampleInput1">
        <b-form-input id="exampleInput1"
                      type="text"
                      v-model="form.username"
                      required
                      placeholder="Enter username">
        </b-form-input>
      </b-form-group>
      <b-form-group id="exampleInputGroup2"
                    label="Password:"
                    label-for="exampleInput2">
        <b-form-input id="exampleInput2"
                      type="text"
                      v-model="form.password"
                      required
                      placeholder="Enter password">
        </b-form-input>
      </b-form-group>
      <b-form-group id="exampleGroup4">
        <b-form-checkbox-group v-model="form.checked" id="exampleChecks">
          <b-form-checkbox value="me">Check me out</b-form-checkbox>
          <b-form-checkbox value="that">Check that out</b-form-checkbox>
        </b-form-checkbox-group>
      </b-form-group>
      <b-button type="submit" variant="primary">Submit</b-button>
      <b-button type="reset" variant="danger">Reset</b-button>
    </b-form>
  </div>
</template>

<script>
//...
</script>
```
&emsp;&emsp;我现在想实现的就是用户登录成功之后导航到另一个组件，所以我就又写了一个欢迎组件：
```html
<template>
    <div>
        <h1>Welcome!</h1>
    </div>
</template>
```
&emsp;&emsp;记得配置路由：
```javascript
// src/router/index.js

import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/components/Login.vue'
import Information from '@/components/Information.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login
    },
    {
      path: '/information',
      name: 'Information',
      component: Information
    }
  ]
})

```
## 后端：提供 RESTful API
&emsp;&emsp;因为只有后端提供了接口，前端才能调用，所以现在要进行后端开发。RESTful 是现在很流行的 API 设计风格，所以我这里也实践了一下。下面是 controller 的代码，完整源码地址附在文末。
```java
@RestController
@RequestMapping("/api")
public class LoginController {

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam String username,
                        @RequestParam String password) {
        // 简单处理一下，实际开发中肯定是要用到数据库的
        if (username.equals("123") && password.equals("123")) {
            return "successful";
        } else {
            return "failed";
        }
    }
}
```
&emsp;&emsp;后端的 API 现在有了，就差前端调用了。但是没这么简单，接下来就要解决我前面提到的问题。
# 实现 CORS
&emsp;&emsp;在这个 Demo 中前端占用的端口是8080，后端是 8088。这就存在跨域的问题，如果不解决的话后端就没法接收前端的请求。

&emsp;&emsp;我参考了[这个例子](https://github.com/boylegu/SpringBoot-vue/blob/master/src/main/java/com/boylegu/springboot_vue/config/CORSConfig.java)，通过配置 Spring MVC 实现了 CORS：
```java
@Configuration
public class CORSConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALL)
                .allowedMethods(ALL)
                .allowedHeaders(ALL)
                .allowCredentials(true);
    }
}
```
&emsp;&emsp;后端配置好了还不行，前端也要有一些配置，要用 axios 顺利地发送请求并保证后端能接收到，需要对请求参数做处理。我参考[这个回答](https://segmentfault.com/q/1010000008292792)用 qs 库对请求参数做了处理：
```javascript
qs.stringify({
        'username': this.form.username,
        'password': this.form.password
      })
```
现在只需完善前端调用后端 API 的代码：
```html
// Login.vue
<script>
export default {
  data () {
    return {
      form: {
        username: '',
        password: '',
        checked: []
      },
      show: true
    }
  },
  methods: {
    onSubmit (evt) {
      evt.preventDefault();
      
      // 关键就在于要对参数进行处理
      axios.post('http://localhost:8088/api/login',qs.stringify({
        'username': this.form.username,
        'password': this.form.password
      })).then((response) => {
        var status = response.data;
        if(status === 'successful') {
          this.$router.push('/information');
        } else {
          alert(response.data.message);
        }
        console.log(response);
      }).catch((error) => {
        console.log(response);
      });
    }
  }
}
</script>
```
至此，终于实现了前后端的分离，并且保证前后端能够顺利交互。
# 题外话
## 让 controller 能获取请求参数
&emsp;&emsp;controller 可能无法获取请求参数，[这篇文章](https://my.oschina.net/u/3491123/blog/1593600)提供了一种解决方案。我这个 Demo 中并没有出现 controller 收不到请求参数的问题，但也把这个问题记录下来，以后可能遇上也说不准。
## axios 方法中的 this
&emsp;&emsp;我这个 Demo 中还试着用 axios 发 GET 请求，然后获取后端响应的 JSON 数据。
```html
// Information.vue
<template>
    <div>
        <h1>Welcome!</h1>
        <div>
            <b-button @click="getInfo()">Get your information</b-button>
            <h2 v-if="username !== ''">Your username is: {{ username }}</h2>
            <h2 v-if="email !== ''">Your email is: {{ email }}</h2>
        </div>
    </div>
</template>

<script>
import axios from 'axios'

export default {
    data () {
        return {
            username: '',
            email: ''
        };
    },
    methods: {
        getInfo () {
            axios.get('http://localhost:8088/api/information')
            .then(function(response) {
                this.username = response.data['username'];
                this.email = response.data['email'];
                console.log(response);
            }).catch(function(error) {
                console.log(error);
            });
        }
    }
}
</script>
```
&emsp;&emsp;一开始我是这么写的，乍一看没什么问题，但是 JavaScript 就一直报错：
```
typeError: Cannot set property 'username' of undefined
```
&emsp;&emsp;搞了很久都没有解决，直到看到[这篇文章](https://segmentfault.com/a/1190000012533993)，才明白原来是`this`作用域的问题（JavaScript 的`this`是真的复杂啊！！！）。改成下面这样就没问题了：
```javascript
            axios.get('http://localhost:8088/api/information')
            .then((response) => {
                this.username = response.data['username'];
                this.email = response.data['email'];
                console.log(response);
            }).catch((error) => {
                console.log(error);
            });
```
后来 Stack Overflow 上有人说不用箭头函数也行，只需提前把指向 Vue 实例的`this`保存在一个变量就行了：
```javascript
            var vue = this;
            axios.get('http://localhost:8088/api/information')
            .then(function (response) {
                vue.username = response.data['username'];
                vue.email = response.data['email'];
                console.log(response);
            }).catch((error) => {
                console.log(error);
            });
```
经实践，这样也是可以的。



