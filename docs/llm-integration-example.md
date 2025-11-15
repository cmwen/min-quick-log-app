# LLM Integration Examples

This document provides examples of how to use the Location Map's LLM-friendly JSON export with various Large Language Models for advanced location analysis.

## Export Format

The Location Map exports data in a structured JSON format designed for easy LLM consumption:

```json
{
  "entries": [
    {
      "id": 1,
      "timestamp": "2024-11-15T08:30:00Z",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "location": "Blue Bottle Coffee, San Francisco",
      "tags": ["coffee", "morning", "work"]
    },
    {
      "id": 2,
      "timestamp": "2024-11-15T12:00:00Z",
      "latitude": 37.7849,
      "longitude": -122.4094,
      "location": "Tech Conference Center, San Francisco",
      "tags": ["conference", "networking", "tech"]
    },
    {
      "id": 3,
      "timestamp": "2024-11-15T18:30:00Z",
      "latitude": 37.7649,
      "longitude": -122.4294,
      "location": "Home",
      "tags": ["dinner", "family", "relax"]
    }
  ],
  "metadata": {
    "total_entries": 3,
    "exported_at": "2024-11-15T20:00:00Z"
  }
}
```

## Use Cases

### 1. Travel Pattern Analysis

**Prompt:**
```
Analyze this location data and identify my travel patterns. 
Look for:
- Most frequently visited places
- Time spent at different locations
- Daily routines or patterns
- Unusual or outlier visits

[Paste JSON data]
```

**Expected Output:**
- List of top locations with visit frequency
- Time-of-day patterns (morning coffee spots, evening locations)
- Weekday vs. weekend patterns
- Suggestions for optimizing routes

### 2. Personal Travel Journal Generation

**Prompt:**
```
Create a travel journal entry based on this location data. 
Write in a narrative style, connecting the locations and activities 
to tell the story of my day/week/month.

[Paste JSON data]
```

**Expected Output:**
- Narrative description of activities
- Insights about lifestyle and habits
- Connections between locations and activities
- Memorable moments highlighted

### 3. Location Recommendations

**Prompt:**
```
Based on my location history and the places I frequently visit, 
suggest 5 new places I might enjoy in the same area. 
Consider the tags associated with my visits.

[Paste JSON data]
```

**Expected Output:**
- Personalized recommendations
- Reasoning based on tags (e.g., "You visit coffee shops in the morning")
- Distance from frequently visited locations
- Similar establishments or experiences

### 4. Productivity Analysis

**Prompt:**
```
Analyze my work-related locations and provide insights about my 
productivity patterns. Focus on:
- Time spent at work locations
- Commute patterns
- Work-life balance indicators
- Focus vs. social activities

Filter for entries tagged with: work, office, meeting, conference

[Paste JSON data]
```

**Expected Output:**
- Work hours analysis
- Commute time calculations
- Work location diversity
- Balance between focused work and meetings

### 5. Health & Wellness Insights

**Prompt:**
```
Analyze my movement patterns from a health perspective:
- Variety of locations (sedentary vs. active lifestyle)
- Outdoor vs. indoor activities
- Social activities frequency
- Exercise or recreation patterns

[Paste JSON data]
```

**Expected Output:**
- Activity diversity score
- Social engagement level
- Outdoor time estimation
- Wellness recommendations

### 6. Memory Recall Assistant

**Prompt:**
```
Based on this location data, help me remember:
"Where was I on November 15th around lunchtime?"
or
"When was the last time I went to [specific location]?"

[Paste JSON data]
```

**Expected Output:**
- Specific location and time
- Associated tags for context
- Related activities or events
- Nearby locations visited around the same time

## Integration Methods

### Direct API Integration (Future)

```kotlin
// Potential future implementation
class LLMLocationAnalyzer(private val apiKey: String) {
    
    suspend fun analyzePatterns(entries: List<LocationEntry>): AnalysisResult {
        val json = exportEntriesAsJson(entries)
        val prompt = """
            Analyze these location patterns and provide insights:
            $json
        """.trimIndent()
        
        return callLLMAPI(prompt)
    }
    
    suspend fun generateJournal(entries: List<LocationEntry>, dateRange: String): String {
        val json = exportEntriesAsJson(entries)
        val prompt = """
            Create a travel journal for $dateRange based on:
            $json
        """.trimIndent()
        
        return callLLMAPI(prompt)
    }
}
```

### Manual Copy-Paste Workflow

Current approach for maximum privacy and flexibility:

1. Open Location Map in the app
2. Filter by desired date range
3. Tap menu → "Export as JSON (LLM-friendly)"
4. Share to your preferred app (notes, email, etc.)
5. Copy JSON content
6. Paste into your LLM of choice (ChatGPT, Claude, Gemini, etc.)
7. Add your specific question or prompt
8. Review and save the insights

### Privacy-Preserving Approach

For sensitive location data:

1. Export filtered subset (e.g., only public places, exclude home)
2. Use local LLM (like Ollama or LM Studio)
3. Anonymize location labels if needed
4. Review exported JSON before sharing
5. Delete shared data after analysis

## LLM-Specific Examples

### ChatGPT (OpenAI)

```
I have location data from my daily activities. Please analyze the 
patterns and provide insights about:
1. My most visited locations
2. Time of day patterns
3. Work-life balance
4. Suggestions for optimization

Here's the data:
[paste JSON]
```

### Claude (Anthropic)

```
I'm sharing my location history in JSON format. I'd like you to:
- Identify any interesting patterns
- Suggest improvements to my daily routine
- Highlight potential time-wasters or inefficiencies
- Provide a weekly summary

<location_data>
[paste JSON]
</location_data>
```

### Gemini (Google)

```
Analyze this location tracking data:

```json
[paste JSON]
```

Questions:
1. What are my top 3 locations?
2. What time do I usually visit coffee shops?
3. How diverse is my daily routine?
4. Any suggestions for better time management?
```

## Advanced Scenarios

### Multi-Day Trip Analysis

Export a week or month of data and ask:
- Create a day-by-day itinerary summary
- Calculate total distance traveled
- Identify the best restaurants/attractions
- Generate a photo location checklist

### Habit Formation

Track a specific habit over time:
- Gym visits frequency and timing
- Coffee shop productivity analysis
- Social location patterns
- Consistency metrics

### Cost Estimation

Combined with receipt tracking:
- Spending patterns by location type
- Most expensive areas/activities
- Budget optimization suggestions
- Value-for-money analysis

## Privacy & Security Best Practices

1. **Review before sharing**: Always check the exported JSON before sending to external services
2. **Use date filters**: Export only the timeframe you're comfortable sharing
3. **Anonymize if needed**: Replace specific location names with generic labels
4. **Prefer local LLMs**: For sensitive data, use on-device or local network LLMs
5. **Delete after use**: Remove shared location data from external services after analysis
6. **Read LLM privacy policies**: Understand how different services handle your data

## Extending the Feature

Potential enhancements for deeper LLM integration:

1. **Built-in prompts**: Pre-configured analysis templates
2. **Local LLM support**: On-device analysis with privacy
3. **Automated insights**: Background analysis with notifications
4. **Multi-format export**: Include photos, notes, weather data
5. **Comparison views**: Compare different time periods
6. **Shareable reports**: Generate formatted analysis reports
7. **Integration APIs**: Direct connection to LLM services with user consent

## Conclusion

The LLM-friendly export format enables powerful location analysis while maintaining user control and privacy. The structured JSON format is designed to be easily understood by language models, leading to better insights and more useful analysis.

For questions or suggestions about LLM integration, please open an issue on GitHub.
