<template>
  <div class="signin flex-col">
    <form
      action="/signin"
      method="POST"
      name="signinForm"
      @submit.prevent="signin"
    >
      <div class="mx-5">
        <div class="mb-4 flex flex-col">
          <VTextField
            id="email"
            v-model="email"
            :placeholder="$t('signin.email')"
            name="email"
            class="mb-3"
          >
            <template #icon>
              <div class="my-auto h-4.5">
                <VIcon
                  icon="nda-email"
                  :alt="$t('app.title')"
                  size="large"
                />
              </div>
            </template>
            <template #error>
              {{ errors.email }}
            </template>
          </VTextField>
        </div>
        <div class="flex flex-col">
          <VTextField
            id="password"
            v-model="password"
            :placeholder="$t('signin.password')"
            type="password"
            name="password"
            class="mb-2"
          >
            <template #icon>
              <div class="h-5 w-5">
                <VIcon
                  icon="nda-padlock"
                  :alt="$t('app.title')"
                  size="large"
                />
              </div>
            </template>
            <template #error>
              {{ error ? message : errors.password }}
            </template>
          </VTextField>
        </div>
        <div class="mb-9 mt-7 text-end text-xs">
          <NuxtLink to="/reset/password/email">
            {{ $t('signin.forgotPassword') }}
          </NuxtLink>
        </div>
      </div>
      <VButton
        type="submit"
        class="h-12 bg-chinese-silver mb-6"
      >
        {{ $t('signin.button') }}
      </VButton>
    </form>
  </div>
</template>

<script lang="ts" setup>
  import { useForm, useField } from 'vee-validate'
  import { toTypedSchema } from '@vee-validate/zod'
  import * as zod from 'zod'

  definePageMeta({
    layout: 'auth',
    middleware: 'guest'
  })

  const { t } = useI18n()
  const { apiSecretKey } = useRuntimeConfig()
  const message = ref('')

  const validationSchema = toTypedSchema(
    zod.object({
      email: zod
        .string({
          required_error: t('common.error.required')
        })
        .email(t('common.error.invalidEmail'))
        .nonempty(t('common.error.required')),
      password: zod
        .string({
          required_error: t('common.error.required')
        })
        .nonempty(t('common.error.required'))
    })
  )

  const { handleSubmit, errors } = useForm({
    validationSchema
  })

  const { value: email } = useField<string | number | undefined>('email')
  const { value: password } = useField<string>('password')

  const body = computed(() => ({
    email: email.value,
    password: password.value.trim()
  }))

  watch([email, password], () => {
    if (message.value) {
      message.value = ''
    }
  })

  const { error, execute } = await useFetch(useApiPath().SIGNIN, {
    method: 'post',
    immediate: false,
    watch: false,
    body,
    credentials: 'include',
    headers: { apiKey: apiSecretKey }
  })

  const onSuccess = async () => {
    await execute()

    if (!error.value) {
      useState('isLoggedin').value = true
      window.location.reload()
    } else if (error.value.statusCode === 403) {
      message.value = t('signin.error.invalidCredentials')
    } else if (error.value.statusCode === 401) {
      message.value = t('signin.error.invalidRole')
    } else {
      message.value = t('signin.error.invalidRole')
    }
  }

  const signin = handleSubmit(onSuccess)
</script>
