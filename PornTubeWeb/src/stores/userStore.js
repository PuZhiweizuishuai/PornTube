import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        userData: (localStorage.getItem('user') != null &&
        localStorage.getItem('user') !== 'undefined' &&
        localStorage.getItem('user') !== '')
          ? JSON.parse(localStorage.getItem('user')) : null
    }),
    actions: {
        setUserData(data) {
            // 同时将用户信息持久化到 localStorage
            localStorage.setItem('user', JSON.stringify(data))
            this.userData = data;
        },
        deleteUserData() {
            localStorage.removeItem('user')
            this.userData = null;
        }
    },
    getters: {
        getUserData: (state) => state.userData
    }
});
    