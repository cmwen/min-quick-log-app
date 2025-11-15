# Location Map Feature

The Location Map feature provides a visual representation of all logged locations on an interactive OpenStreetMap, with powerful filtering and export capabilities designed to be LLM-friendly.

## Overview

This feature allows users to:
- View all logged entries on an interactive map
- Filter entries by date range
- View a chronological timeline of locations
- Export location data in LLM-friendly formats (JSON and CSV)

## Key Components

### LocationMapActivity
The main activity that displays the map interface with the following features:
- Interactive OpenStreetMap view powered by osmdroid
- Material date range picker for filtering entries
- Timeline dialog showing chronological location visits
- Export functionality for JSON and CSV formats

### LocationMapViewModel
Manages the data layer:
- Observes and filters log entries from the repository
- Handles date range filtering
- Generates structured export data

### Data Models
- `LocationEntry`: Simplified data class for map display
- `DateFilter`: Encapsulates start and end date for filtering

## User Interface

### Map View
- Displays markers for each logged location
- Markers show location label (or coordinates if geocoding is unavailable)
- Tap markers to see entry details including date and tags
- Multi-touch controls for zoom and pan

### Filter Controls
Located in a Material Card at the bottom of the screen:
- **Date filter text**: Shows current filter status or "All dates"
- **Filter Dates button**: Opens Material date range picker
- **Clear button**: Removes active filters (hidden when no filter applied)
- **Timeline button**: Opens chronological timeline dialog

### Navigation
Accessible from:
- MainActivity toolbar (location icon)
- EntriesOverviewActivity toolbar
- Bottom navigation bar (entries section)

## Export Formats

### JSON Export (LLM-Friendly)
Structured JSON format designed for easy parsing by Large Language Models:

```json
{
  "entries": [
    {
      "id": 1,
      "timestamp": "2024-11-15T10:30:00Z",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "location": "Downtown, San Francisco",
      "tags": ["coffee", "meeting", "work"]
    }
  ],
  "metadata": {
    "total_entries": 1,
    "exported_at": "2024-11-15T15:45:00Z"
  }
}
```

**Benefits for LLM integration:**
- Clear structure with metadata section
- Human-readable timestamps in ISO 8601 format
- Explicit field names
- Tags as array for easy processing
- Total count and export timestamp for context

**Potential LLM use cases:**
- Analyze travel patterns
- Generate location summaries
- Identify frequently visited places
- Create travel journals
- Suggest similar locations
- Answer questions like "Where was I on [date]?"

### CSV Export
Traditional CSV format for spreadsheet analysis:

```csv
ID,Timestamp,Latitude,Longitude,Location,Tags
1,"2024-11-15T10:30:00Z",37.7749,-122.4194,"Downtown, San Francisco","coffee; meeting; work"
```

## Date Filtering

### Material Date Range Picker
- Modern Material 3 date picker UI
- Select start and end dates
- Calendar view with today highlight
- Remembers previous selection when reopening
- Filter applies immediately after selection

### Filter Behavior
- Filters entries where `createdAt` is between start and end dates (inclusive)
- Visual indicator shows active filter in readable format (e.g., "Jan 1, 2024 – Dec 31, 2024")
- Clear button removes filter and shows all entries

## Timeline View

### Features
- Chronological list of all visible entries (respects date filter)
- Shows date/time, location, and tags for each entry
- Uses emoji indicators: 📍 for location, 🏷️ for tags
- Formatted for easy reading
- Share button to export timeline as text

### Format
```
Nov 15, 2024 10:30
📍 Downtown, San Francisco
🏷️ coffee, meeting, work

Nov 15, 2024 14:45
📍 Home
🏷️ relax, reading
```

## Technical Details

### Dependencies
- **osmdroid**: Open-source OpenStreetMap library (v6.1.18)
  - Chosen to avoid Google Maps API key requirements
  - Fully offline-capable (caches tiles)
  - Lighter weight than Google Maps SDK

### Permissions
- `INTERNET`: Required to download map tiles
- `ACCESS_NETWORK_STATE`: Check network availability
- `WRITE_EXTERNAL_STORAGE`: Map tile caching (Android ≤ 12)

### Data Source
- Reads from existing `EntryEntity` with `latitude`, `longitude`, and `locationLabel` fields
- No database schema changes required
- Reuses `LocationProvider` infrastructure

### Theme Support
- Respects system theme (light/dark)
- Map tiles adjust based on theme
- Material 3 components throughout

## Localization

Fully localized for:
- English (en)
- Spanish (es)
- French (fr)

All UI strings, button labels, and dialog messages are translated.

## Future Enhancements

Possible improvements:
1. **Clustering**: Group nearby markers for better performance with many entries
2. **Heat map**: Show location visit frequency as a heat map overlay
3. **Route lines**: Draw lines between consecutive location visits
4. **Location categories**: Color-code markers by tags or categories
5. **Search**: Search for specific locations or tags
6. **Statistics**: Show most visited places, total distance traveled
7. **Geofencing**: Set up location-based reminders
8. **Offline mode**: Enhanced offline map functionality
9. **Custom map styles**: Allow users to choose map appearance
10. **Integration with LLM services**: Direct API integration for automated insights

## Privacy Considerations

- All location data stays on device
- Export requires explicit user action (share dialog)
- User controls what data is exported via date filtering
- No automatic cloud sync or third-party sharing
- Map tiles are downloaded from public OpenStreetMap servers (standard privacy policy applies)
