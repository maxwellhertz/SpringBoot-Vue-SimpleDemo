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
            .then((response) => {
                this.username = response.data['username'];
                this.email = response.data['email'];
                console.log(response);
            }).catch((error) => {
                console.log(error);
            });
        }
    }
}
</script>

