export default defineNuxtRouteMiddleware(() => {
  const isLoggedin = useState('isLoggedin')

  if (isLoggedin.value) {
    return navigateTo('/member')
  }
})
