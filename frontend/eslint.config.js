// @ts-check
const eslint = require('@eslint/js');
const { defineConfig } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');
const simpleImportSort = require('eslint-plugin-simple-import-sort');

module.exports = defineConfig([
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    plugins: {
      '@typescript-eslint': tseslint.plugin,
      'simple-import-sort': simpleImportSort
    },
    rules: {
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase'
        }
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case'
        }
      ],
      '@angular-eslint/prefer-standalone': 'error',
      'quotes': [ 'warn', 'single', { "allowTemplateLiterals": true } ],
      'semi': [ 'warn', 'never' ],
      'comma-dangle': [ 'warn', 'never' ],
      'comma-spacing': [ 'warn', { before: false, after: true } ],
      'no-empty-function': 'warn',
      'object-curly-spacing': [ 'warn', 'always' ],
      'array-bracket-spacing': [ 'warn', 'always' ],
      'no-mixed-spaces-and-tabs': 'warn',
      'key-spacing': [ 'warn', { beforeColon: false, afterColon: true } ],
      'indent': [ 'warn', 2 ],
      'space-in-parens': [ 'warn', 'never' ],
      'no-multiple-empty-lines': [ 'warn', { max: 1, maxEOF: 0, maxBOF: 0 } ],
      'space-before-function-paren': [ 'warn', 'always' ],
      'no-trailing-spaces': 'warn',
      'no-whitespace-before-property': 'warn',
      'no-multi-spaces': [ 'warn', { ignoreEOLComments: false } ],
      'no-magic-numbers': [ 'error', { detectObjects: true, ignoreDefaultValues: true, ignoreClassFieldInitialValues: true, ignoreArrayIndexes: true, enforceConst: true } ],
      'space-infix-ops': [ 'warn', { int32Hint: false } ],
      'camelcase': 'error',
      '@typescript-eslint/explicit-function-return-type': [ 'error' ],
      'eol-last': [ 'error', 'always' ],
      'no-debugger': 'error',
      'no-console': 'warn',
      'simple-import-sort/imports': 'error',
      'simple-import-sort/exports': 'error',
      'max-params': [ 'error', 4 ],
      'no-nested-ternary': 'error',
      'no-underscore-dangle': [ 'error', { enforceInMethodNames: true, enforceInClassFields: true, allowInArrayDestructuring: false, allowInObjectDestructuring: false } ],
      '@typescript-eslint/naming-convention': [
        'error', {
          selector: 'interface',
          format: [ 'PascalCase' ],
          custom: { regex: '^I[A-Z]', match: false }
        }
      ]
    }
  },
  {
    files: [ '**/*.html' ],
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility
    ],
    rules: {
      '@angular-eslint/template/prefer-self-closing-tags': [ 'warn' ],
      '@angular-eslint/template/prefer-ngsrc': [ 'warn' ],
      '@angular-eslint/template/prefer-control-flow': [ 'warn' ]
    }
  },
  {
    files: ['**/*.js', '**/*.ts'],
    plugins: {
      '@typescript-eslint': tseslint.plugin,
      'simple-import-sort': simpleImportSort
    },
    rules: {
      'quotes': [ 'warn', 'single' ],
      'semi': [ 'warn', 'never' ],
      'comma-dangle': [ 'warn', 'never' ],
      'comma-spacing': [ 'warn', { before: false, after: true } ],
      'no-empty-function': 'warn',
      'object-curly-spacing': [ 'warn', 'always' ],
      'array-bracket-spacing': [ 'warn', 'always' ],
      'no-mixed-spaces-and-tabs': 'warn',
      'key-spacing': [ 'warn', { beforeColon: false, afterColon: true } ],
      'indent': [ 'warn', 2 ],
      'space-in-parens': [ 'warn', 'never' ],
      'no-multiple-empty-lines': [ 'warn', { max: 1, maxEOF: 0, maxBOF: 0 } ],
      'space-before-function-paren': [ 'warn', 'always' ],
      'no-trailing-spaces': 'warn',
      'no-whitespace-before-property': 'warn',
      'no-multi-spaces': [ 'warn', { ignoreEOLComments: false } ],
      'no-magic-numbers': [ 'error', { detectObjects: true, ignoreDefaultValues: true, ignoreClassFieldInitialValues: true, ignoreArrayIndexes: true, enforceConst: true } ],
      'space-infix-ops': [ 'warn', { int32Hint: false } ],
      'camelcase': 'error',
      '@typescript-eslint/explicit-function-return-type': [ 'error' ],
      'eol-last': [ 'error', 'always' ],
      'no-debugger': 'error',
      'no-console': 'warn',
      'simple-import-sort/imports': 'error',
      'simple-import-sort/exports': 'error',
      'max-params': [ 'error', 4 ],
      'no-nested-ternary': 'error',
      'no-underscore-dangle': [ 'error', { enforceInMethodNames: true, enforceInClassFields: true, allowInArrayDestructuring: false, allowInObjectDestructuring: false } ]
    }
  }
])
