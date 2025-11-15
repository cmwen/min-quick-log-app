# Location Map UI Mockup Description

This document describes the visual appearance and layout of the Location Map feature for designers and developers.

## Activity Structure

```
┌─────────────────────────────────────────┐
│ ←  Location Map              ⋮          │  ← Toolbar
├─────────────────────────────────────────┤
│                                         │
│                                         │
│         🗺️  Interactive Map View       │
│                                         │
│    📍 Markers show logged locations    │
│                                         │
│   (OpenStreetMap with zoom/pan)        │
│                                         │
│                                         │
│                                         │
│                                         │
├─────────────────────────────────────────┤
│ ┌───────────────────────────────────┐ │
│ │  📅  Nov 1, 2024 – Nov 15, 2024  │ │  ← Filter Card
│ │  42 entries                       │ │
│ │                                   │ │
│ │  [Filter Dates] [Timeline]       │ │
│ └───────────────────────────────────┘ │
├─────────────────────────────────────────┤
│  🏠  📊  🏷️                            │  ← Bottom Nav
└─────────────────────────────────────────┘
```

## Components Breakdown

### 1. Toolbar (Material AppBar)
```
┌─────────────────────────────────────────┐
│ ←  Location Map              ⋮          │
└─────────────────────────────────────────┘
  ↑                              ↑
  Back button                   Overflow menu
                                (Export JSON/CSV)
```

- **Left**: Back/navigation icon (arrow)
- **Center**: "Location Map" title
- **Right**: Overflow menu (⋮)
  - Export as JSON (LLM-friendly)
  - Export as CSV

### 2. Map View (OSMdroid)
```
┌─────────────────────────────────────────┐
│                                         │
│         📍                              │
│    Coffee Shop                          │
│  Nov 15, 10:30 AM                      │
│  coffee, morning                        │
│                                         │
│              📍                         │
│           Office                        │
│                     📍                  │
│                   Park                  │
│                                         │
└─────────────────────────────────────────┘
```

**Map Features:**
- Interactive OpenStreetMap tiles
- Pin markers (📍) at each logged location
- Marker info windows show:
  - Location label
  - Date/time
  - Associated tags
- Multi-touch controls (zoom, pan)
- Automatic centering on entries

**Empty State:**
```
┌─────────────────────────────────────────┐
│                                         │
│              🗺️                         │
│                                         │
│    No location entries to display       │
│                                         │
│      Log some entries with location     │
│           to see them here!             │
│                                         │
└─────────────────────────────────────────┘
```

### 3. Filter Card (Material Card)
```
┌───────────────────────────────────────┐
│ 📅  Nov 1, 2024 – Nov 15, 2024   ✕   │ ← Date range + Clear button
│ 42 entries                            │ ← Entry count
│                                       │
│ ┌──────────────┐  ┌──────────────┐  │
│ │ Filter Dates │  │   Timeline   │  │ ← Action buttons
│ └──────────────┘  └──────────────┘  │
└───────────────────────────────────────┘
```

**States:**

**No Filter Active:**
```
┌───────────────────────────────────────┐
│ All dates                             │
│ 128 entries                           │
│                                       │
│ ┌──────────────┐  ┌──────────────┐  │
│ │ Filter Dates │  │   Timeline   │  │
│ └──────────────┘  └──────────────┘  │
└───────────────────────────────────────┘
```

**Filter Active:**
```
┌───────────────────────────────────────┐
│ Jan 1, 2024 – Jan 31, 2024       ✕   │ ← Clear button visible
│ 23 entries                            │
│                                       │
│ ┌──────────────┐  ┌──────────────┐  │
│ │ Filter Dates │  │   Timeline   │  │
│ └──────────────┘  └──────────────┘  │
└───────────────────────────────────────┘
```

### 4. Bottom Navigation
```
┌─────────────────────────────────────────┐
│   🏠        📊         🏷️                │
│  Record  Entries    Tags                │
└─────────────────────────────────────────┘
```

- **Record**: Main entry screen
- **Entries**: Overview (current - highlighted)
- **Tags**: Tag manager

## Dialogs

### Date Range Picker
```
┌─────────────────────────────────────────┐
│  Select Date Range                      │
├─────────────────────────────────────────┤
│                                         │
│     November 2024                       │
│  S  M  T  W  T  F  S                   │
│                 1  2                    │
│  3  4  5  6  7  8  9                   │
│ 10 11 12 13 14 15 16                   │
│ 17 18 19 20 21 22 23                   │
│ 24 25 26 27 28 29 30                   │
│                                         │
│  Start: Nov 1    End: Nov 15           │
│                                         │
│           [Cancel]  [OK]                │
└─────────────────────────────────────────┘
```

Material DateRangePicker with:
- Calendar view
- Selected range highlighted
- Month/year navigation
- Start/End date display
- Cancel/OK actions

### Timeline Dialog
```
┌─────────────────────────────────────────┐
│  Location Timeline                  ✕   │
├─────────────────────────────────────────┤
│                                         │
│  Nov 15, 2024 08:30                    │
│  📍 Blue Bottle Coffee                 │
│  🏷️  coffee, morning, work             │
│                                         │
│  Nov 15, 2024 12:00                    │
│  📍 Tech Conference Center             │
│  🏷️  conference, networking            │
│                                         │
│  Nov 15, 2024 18:30                    │
│  📍 Home                               │
│  🏷️  dinner, family, relax             │
│                                         │
│           [Share]  [OK]                 │
└─────────────────────────────────────────┘
```

Scrollable list with:
- Chronological order (oldest to newest)
- Emoji indicators (📍🏷️)
- Date/time stamps
- Location labels
- Tag lists
- Share and OK buttons

## Color Scheme (Material 3)

### Light Theme
- **Primary**: Material Blue (#6750A4)
- **Surface**: White (#FFFFFF)
- **Surface Variant**: Light Gray (#E7E0EC)
- **On Surface**: Dark Gray (#1C1B1F)
- **Marker Color**: Material Red (#B3261E)

### Dark Theme
- **Primary**: Material Purple (#D0BCFF)
- **Surface**: Dark Gray (#1C1B1F)
- **Surface Variant**: Dark Purple (#49454F)
- **On Surface**: Light Gray (#E6E1E5)
- **Marker Color**: Material Light Red (#F2B8B5)

## Typography (Material 3)

- **Title Large**: 22sp, Medium weight (Toolbar title)
- **Body Large**: 16sp, Regular (Date filter text)
- **Body Medium**: 14sp, Regular (Entry count)
- **Label Large**: 14sp, Medium (Button text)
- **Body Small**: 12sp, Regular (Marker snippets)

## Spacing

- **Card Margin**: 16dp all sides
- **Card Padding**: 16dp internal
- **Button Spacing**: 8dp between buttons
- **Text Padding**: 8dp vertical between text elements
- **Marker Icon Size**: 48x48dp

## Elevation (Material 3)

- **Toolbar**: Level 0 (0dp)
- **Filter Card**: Level 2 (4dp)
- **Dialogs**: Level 3 (8dp)
- **Bottom Nav**: Level 2 (4dp)

## Animations

- **Map Zoom**: Smooth interpolation (300ms)
- **Marker Tap**: Ripple effect + info window slide up (200ms)
- **Card Reveal**: Slide up from bottom (400ms)
- **Filter Apply**: Fade markers out/in (150ms)
- **Dialog Open**: Scale up from center (250ms)

## Accessibility

- **Touch Targets**: Minimum 48x48dp
- **Contrast Ratios**: WCAG AA compliant
- **Screen Reader Labels**: All interactive elements
- **Keyboard Navigation**: Full support
- **Text Scaling**: Respects system font size

## Responsive Behavior

### Portrait Mode (Default)
- Filter card at bottom
- Map fills majority of screen
- Bottom nav always visible

### Landscape Mode
- Filter card position may adjust
- Map takes full height
- Navigation remains accessible

### Tablet (>600dp)
- Wider filter card with more padding
- Larger map markers
- Side-by-side button layout in filter card

## Example Screenshots Description

### Main View
"Screenshot shows the map view with multiple pin markers scattered across San Francisco. A white Material card floats near the bottom showing '42 entries' and two buttons. The toolbar at top has a back arrow on left and menu icon on right. Bottom navigation shows 3 icons with 'Entries' highlighted in blue."

### Filtered View
"Map displays only 5 markers, all in downtown area. The filter card shows 'Nov 10 – Nov 12, 2024' with a small X button for clearing. The entry count reads '5 entries'. Timeline button is enabled."

### Timeline Dialog
"White dialog box overlays the map, titled 'Location Timeline'. Contains 3 entries listed vertically, each with timestamp, location emoji, location name, tag emoji, and comma-separated tags. Share button in bottom left, OK button in bottom right."

---

**Note**: This is a textual description. Actual visual mockups would be created using Figma, Sketch, or similar design tools.
