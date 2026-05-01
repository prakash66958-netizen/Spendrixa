---
name: Professional Financial Interface
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#45464d'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#006c49'
  on-secondary: '#ffffff'
  secondary-container: '#6cf8bb'
  on-secondary-container: '#00714d'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#001a42'
  on-tertiary-container: '#3980f4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#d8e2ff'
  tertiary-fixed-dim: '#adc6ff'
  on-tertiary-fixed: '#001a42'
  on-tertiary-fixed-variant: '#004395'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  h1:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.25'
    letterSpacing: -0.01em
  h2:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  h3:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1.2'
    letterSpacing: 0.02em
  numeric:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: '1'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 24px
  container-max: 1440px
---

## Brand & Style

This design system is built upon the pillars of **Institutional Trust**, **Numerical Precision**, and **Clarity**. It is designed for high-stakes financial environments where data density must be balanced with cognitive ease. 

The aesthetic follows a **Corporate / Modern** style, characterized by a structured grid, generous whitespace to reduce "data fatigue," and a focus on functional elegance. It avoids unnecessary ornamentation, ensuring that every element serves a clear purpose in the user's financial workflow. The result is a secure, authoritative environment that feels both high-tech and dependable.

## Colors

The color palette is engineered to communicate stability and success.
- **Primary (Deep Blue):** Used for navigation, core branding, and primary actions to evoke a sense of institutional security and depth.
- **Secondary (Emerald Green):** Reserved strictly for positive financial indicators, growth metrics, and "success" states.
- **Tertiary (Action Blue):** A lighter blue used for links and secondary interactive elements to distinguish them from structural primary elements.
- **Neutrals (Subtle Grays):** A cool-toned gray scale used for borders, secondary text, and background layering to maintain a clean, organized interface.
- **Feedback Palette:** Includes a sharp Crimson (#EF4444) for negative trends/errors and Amber (#F59E0B) for warnings or pending transactions.

## Typography

The design system utilizes **Inter** exclusively for its exceptional legibility and neutral tone. To support data-heavy financial views, the system prioritizes "Tabular Numbers" (tnum) to ensure that columns of figures align perfectly for easy scanning.

Hierarchy is established through weight and color rather than drastic size changes, maintaining a compact but readable layout. Use the `numeric` variant for all account balances and transaction lists to ensure maximum precision.

## Layout & Spacing

This design system employs a **Fixed Grid** model for desktop views, centering content within a 1440px container to ensure readability on wide monitors. The rhythm is based on an **8px linear scale**, providing a predictable and balanced structure.

The 12-column grid utilizes 24px gutters. For data-dense dashboards, a "Compact Mode" may be triggered which reduces `md` and `lg` spacing by 25%, allowing more information to be visible above the fold without sacrificing the structural integrity of the layout.

## Elevation & Depth

Visual hierarchy is managed through **Tonal Layers** and **Ambient Shadows**. Instead of heavy shadows, the system uses subtle, diffused elevation to separate functional areas:

1.  **Level 0 (Background):** The base interface color (#F8FAFC).
2.  **Level 1 (Cards/Sections):** White surfaces with a very soft, 4% opacity shadow (0px 2px 4px rgba(15, 23, 42, 0.04)) and a 1px subtle gray border.
3.  **Level 2 (Popovers/Dropdowns):** White surfaces with a 12% opacity shadow (0px 10px 15px rgba(15, 23, 42, 0.12)) to indicate interactivity and focus.

This approach creates a flat, professional look that uses depth only to provide "hints" of interactivity and stacking order.

## Shapes

The design system uses a **Rounded** shape language to soften the industrial feel of financial data.
- **Standard Elements:** Buttons, Input fields, and Cards use a 0.5rem (8px) corner radius.
- **Large Containers:** Modals and primary content areas use a 1rem (16px) radius.
- **Data Indicators:** Small badges or tags may use a pill-shape (full rounding) to differentiate them from actionable buttons.

## Components

### Buttons
Primary buttons use the Deep Blue background with white text. Secondary buttons use a subtle gray outline. Success actions (e.g., "Complete Transfer") may utilize the Emerald Green to provide a positive psychological reinforcement.

### Input Fields
Inputs are defined by a 1px border (#E2E8F0) and 8px rounding. Focus states must use a 2px Deep Blue ring with a subtle offset to ensure accessibility and clarity during data entry.

### Data Tables
Tables are the heart of this design system. They feature:
- Sticky headers with a subtle bottom border.
- Alternating row highlights (zebra striping) at 2% opacity.
- Right-aligned numeric columns for financial comparison.
- "Positive" and "Negative" status chips utilizing the Emerald and Crimson palettes respectively.

### Cards
Cards are used to group related financial metrics (e.g., Portfolio Summary). They should always include a 1px border and the Level 1 shadow defined in the Elevation section.

### Additional Components
- **Trend Sparklines:** Micro-charts embedded in lists to show 7-day financial trends.
- **Status Badges:** Small, high-contrast labels for "Pending," "Cleared," or "Flagged" transactions.
- **Progress Steppers:** Horizontal indicators for multi-step financial workflows (e.g., Loan Application).