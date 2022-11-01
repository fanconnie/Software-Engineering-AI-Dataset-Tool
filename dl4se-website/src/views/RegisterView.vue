<template>
  <div id="register">
    <h1 class="text-center">Register</h1>
    <b-container>
      <b-form @submit.prevent.stop="register" novalidate>
        <b-form-row>
          <b-form-group label-for="email">
            <template #label>
              Email
              <b-icon-asterisk font-scale="0.35" shift-v="16" class="text-danger" />
            </template>
            <b-form-input
              id="email"
              name="email"
              type="email"
              autocomplete="email"
              v-model.trim="form.email"
              :disabled="submitted"
              :state="v$.form.email.$dirty ? !v$.form.email.$invalid : null"
              placeholder="example@email.com"
              autofocus
              @blur="autofillOrganisation"
            />
          </b-form-group>
        </b-form-row>
        <b-form-row>
          <b-form-group label-for="password">
            <template #label>
              Password
              <b-icon-asterisk font-scale="0.35" shift-v="16" class="text-danger" />
            </template>
            <b-form-input-password
              id="password"
              name="password"
              autocomplete="new-password"
              v-model.trim="form.password"
              :disabled="submitted"
              :state="v$.form.password.$dirty ? !v$.form.password.$invalid : null"
            />
            <b-form-text v-if="v$.form.password.$invalid">
              <b-icon-exclamation-octagon />
              <span>Must be at least 6 characters long, containing a number, uppercase and lowercase character.</span>
            </b-form-text>
          </b-form-group>
        </b-form-row>
        <b-form-row>
          <b-form-group label-for="confirm">
            <template #label>
              Confirm Password
              <b-icon-asterisk font-scale="0.35" shift-v="16" class="text-danger" />
            </template>
            <b-form-input-password
              id="confirm"
              name="confirm"
              autocomplete="new-password"
              v-model.trim="form.confirm"
              :disabled="submitted"
              :state="v$.form.confirm.$dirty ? !v$.form.confirm.$invalid : null"
            />
            <b-form-invalid-feedback v-if="v$.