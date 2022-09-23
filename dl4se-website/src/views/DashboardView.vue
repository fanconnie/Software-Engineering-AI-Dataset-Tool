<template>
  <div id="dashboard">
    <h1 class="d-none">Dashboard</h1>
    <b-container>
      <b-row>
        <b-col>
          <h2>Requested Datasets</h2>
        </b-col>
      </b-row>
      <b-row align-h="center">
        <b-col>
          <b-paginated-table
            :id="taskTable.id"
            :fields="taskTable.fields"
            :controls="['code', 'filters']"
            :primary-key="taskTable.fields[0].key"
            :total-items="taskTable.totalItems"
            :provider="taskProvider"
            :sticky-header="tableHeight"
          >
            <template #controls(code)>
              <b-button :to="{ name: 'code' }" block class="btn-controls">
                <b-icon-plus class="align-middle" font-scale="1.5" />
                <span class="align-middle">Create New Dataset</span>
              </b-button>
            </template>
            <template #controls(filters)>
              <b-button v-b-modal.task-filter-select block class="btn-controls">
                <b-icon-filter class="align-middle" font-scale="1.5" />
                <span class="align-middle">Filter Settings</span>
              </b-button>
            </template>
            <template #cell(uuid)="row">
              <b-abbr
                v-if="!$screen.md"
                :title="`<span class='text-monospace'>${row.value}</span>`"
                class="text-monospace"
              >
                {{ row.value.split("-")[0] }}
              </b-abbr>
              <span v-else class="text-monospace">{{ row.value }}</span>
            </template>
            <template #cell(status)="row">
              <b-icon :icon="statusToSquareIcon(row.value)" v-b-tooltip="startCase(row.value)" font-scale="1.25" />
            </template>
            <template #cell(submitted)="row">
              <div class="d-inline-flex align-items-center">
                <template v-if="row.value.submitted">
                  <b-icon-calendar-plus
                    v-b-tooltip.html="`Submitted at:<br />${row.value.submitted.toISOString()}`"
                    font-scale="1.35"
                  />
                </template>
                <template v-if="row.value.started">
                  <b-icon-dash-lg shift-v="-1" />
                  <b-icon-calendar-play
                    v-b-tooltip.html