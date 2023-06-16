import {DBConfig} from "ngx-indexed-db";

export const dbConfig: DBConfig = {
  name: 'mediminderDB',
  version: 3,
  objectStoresMeta: [{
    store: 'medication',
    storeConfig: {keyPath: 'id', autoIncrement: false},
    storeSchema: [
      {name: 'name', keypath: 'name', options: {unique: true}},
      {name: 'typeId', keypath: 'type', options: {unique: false}},
    ]
  }, {
    store: 'medicationType',
    storeConfig: {keyPath: 'id', autoIncrement: false},
    storeSchema: [
      {name: 'name', keypath: 'name', options: {unique: true}},
      {name: 'unit', keypath: 'unit', options: {unique: false}},
      {name: 'individual', keypath: 'individual', options: {unique: false}},
    ]
  }, {
    store: 'cabinet',
    storeConfig: {keyPath: 'id', autoIncrement: false},
    storeSchema: [
      {name: 'medicationId', keypath: 'medicationId', options: {unique: false}},
      {name: 'expiryDate', keypath: 'expiryDate', options: {unique: false}},
      {name: 'units', keypath: 'units', options: {unique: false}},
      {name: 'initialUnits', keypath: 'initialUnits', options: {unique: false}},
    ]
  }, {
    store: 'schedule',
    storeConfig: {keyPath: 'id', autoIncrement: false},
    storeSchema: [
      {name: 'medicationId', keypath: 'medicationId', options: {unique: false}},
      {name: 'period', keypath: 'period', options: {unique: false}},
      {name: 'dose', keypath: 'dose', options: {unique: false}},
      {name: 'recurrence', keypath: 'recurrence', options: {unique: false}},
      {name: 'time', keypath: 'time', options: {unique: false}},
      {name: 'description', keypath: 'description', options: {unique: false}},
    ]
  }, {
    store: 'intake',
    storeConfig: {keyPath: 'id', autoIncrement: false},
    storeSchema: [
      {name: 'scheduleId', keypath: 'scheduleId', options: {unique: false}},
      {name: 'scheduledDate', keypath: 'scheduledDate', options: {unique: false}},
      {name: 'completedDate', keypath: 'completedDate', options: {unique: false}},
    ]
  }],
};
