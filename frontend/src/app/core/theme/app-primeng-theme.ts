import { definePreset } from '@primeuix/themes'
import Aura from '@primeuix/themes/aura'

export const AppPrimeNgPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{zinc.50}',
      100: '{zinc.100}',
      200: '{zinc.200}',
      300: '{zinc.300}',
      400: '{zinc.400}',
      500: '{zinc.500}',
      600: '{zinc.600}',
      700: '{zinc.700}',
      800: '{zinc.800}',
      900: '{zinc.900}',
      950: '{zinc.950}'
    },
    colorScheme: {
      light: {
        primary: {
          color: '{zinc.950}',
          inverseColor: '#ffffff',
          hoverColor: '{zinc.900}',
          activeColor: '{zinc.800}'
        },
        highlight: {
          background: '{zinc.950}',
          focusBackground: '{zinc.700}',
          color: '#ffffff',
          focusColor: '#ffffff'
        },
        surface: {
          0: '#ffffff',
          50: '#f7f7f8',
          100: '#eeeeef',
          200: '#dedee2',
          300: '#c9c9cf',
          400: '#aaaab4',
          500: '#858592',
          600: '#686875',
          700: '#4f4f5c',
          800: '#363640',
          900: '#24242d',
          950: '#141419'
        }
      }
    }
  }
})
