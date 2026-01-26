package com.mobile.habittrackernew.services

import com.mobile.habittrackernew.data.models.Category
import com.mobile.habittrackernew.data.models.StreakInfo
import com.mobile.habittrackernew.data.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AIService @Inject constructor() {

    private val greetings = listOf(
        "Hello! 👋",
        "Hi there! 😊",
        "Hey! Great to see you! 🌟",
        "Welcome back! 💪"
    )

    suspend fun generateResponse(
        userMessage: String,
        streaks: Map<String, StreakInfo>,
        userProfile: UserProfile?
    ): String = withContext(Dispatchers.IO) {
        // Simulate typing delay for natural feel
        delay(Random.nextLong(800, 1500))

        val lowercaseMessage = userMessage.lowercase()
        val currentHour = LocalTime.now().hour
        val userName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "there"

        when {
            // Greetings
            lowercaseMessage.matches(Regex("^(hi|hello|hey|good morning|good afternoon|good evening).*")) ->
                generateGreetingResponse(userName, currentHour, streaks)

            // Schedule related
            lowercaseMessage.contains("schedule") ||
                    lowercaseMessage.contains("routine") ||
                    lowercaseMessage.contains("timetable") ||
                    lowercaseMessage.contains("daily plan") ->
                generateDetailedScheduleResponse(userProfile)

            // Diet related
            lowercaseMessage.contains("diet") ||
                    lowercaseMessage.contains("food") ||
                    lowercaseMessage.contains("eat") ||
                    lowercaseMessage.contains("meal") ||
                    lowercaseMessage.contains("nutrition") ->
                generateDetailedDietResponse(userProfile)

            // Exercise related
            lowercaseMessage.contains("exercise") ||
                    lowercaseMessage.contains("workout") ||
                    lowercaseMessage.contains("fitness") ||
                    lowercaseMessage.contains("gym") ||
                    lowercaseMessage.contains("training") ->
                generateDetailedExerciseResponse()

            // Sleep related
            lowercaseMessage.contains("sleep") ||
                    lowercaseMessage.contains("rest") ||
                    lowercaseMessage.contains("insomnia") ||
                    lowercaseMessage.contains("tired") ->
                generateDetailedSleepResponse(userProfile)

            // Meditation related
            lowercaseMessage.contains("meditat") ||
                    lowercaseMessage.contains("mindful") ||
                    lowercaseMessage.contains("calm") ||
                    lowercaseMessage.contains("stress") ||
                    lowercaseMessage.contains("anxiety") ->
                generateDetailedMeditationResponse()

            // Hydration related
            lowercaseMessage.contains("water") ||
                    lowercaseMessage.contains("hydrat") ||
                    lowercaseMessage.contains("drink") ->
                generateDetailedHydrationResponse()

            // Study related
            lowercaseMessage.contains("study") ||
                    lowercaseMessage.contains("learn") ||
                    lowercaseMessage.contains("focus") ||
                    lowercaseMessage.contains("concentrate") ||
                    lowercaseMessage.contains("exam") ->
                generateDetailedStudyResponse()

            // Motivation related
            lowercaseMessage.contains("motivat") ||
                    lowercaseMessage.contains("inspire") ||
                    lowercaseMessage.contains("encourage") ||
                    lowercaseMessage.contains("help me") ||
                    lowercaseMessage.contains("feeling") ||
                    lowercaseMessage.contains("lazy") ||
                    lowercaseMessage.contains("unmotivated") ->
                generatePersonalizedMotivationResponse(userName, streaks)

            // Progress related
            lowercaseMessage.contains("progress") ||
                    lowercaseMessage.contains("how am i") ||
                    lowercaseMessage.contains("my stats") ||
                    lowercaseMessage.contains("streak") ->
                generateProgressReviewResponse(userName, streaks)

            // Yoga related
            lowercaseMessage.contains("yoga") ||
                    lowercaseMessage.contains("stretch") ||
                    lowercaseMessage.contains("flexibility") ->
                generateYogaResponse()

            // Self care related
            lowercaseMessage.contains("self care") ||
                    lowercaseMessage.contains("self-care") ||
                    lowercaseMessage.contains("mental health") ||
                    lowercaseMessage.contains("wellness") ->
                generateSelfCareResponse()

            // Goal setting
            lowercaseMessage.contains("goal") ||
                    lowercaseMessage.contains("target") ||
                    lowercaseMessage.contains("achieve") ->
                generateGoalSettingResponse(streaks)

            // Tips
            lowercaseMessage.contains("tip") ||
                    lowercaseMessage.contains("advice") ||
                    lowercaseMessage.contains("suggest") ||
                    lowercaseMessage.contains("recommend") ->
                generateRandomTipsResponse()

            // Thank you
            lowercaseMessage.contains("thank") ||
                    lowercaseMessage.contains("thanks") ->
                generateThankYouResponse()

            // Bye
            lowercaseMessage.contains("bye") ||
                    lowercaseMessage.contains("goodbye") ||
                    lowercaseMessage.contains("see you") ->
                generateGoodbyeResponse(userName)

            // Default
            else -> generateSmartDefaultResponse(userMessage, streaks)
        }
    }

    private fun generateGreetingResponse(
        userName: String,
        hour: Int,
        streaks: Map<String, StreakInfo>
    ): String {
        val timeGreeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            hour < 21 -> "Good evening"
            else -> "Good night"
        }

        val activeStreaks = streaks.values.count { it.currentStreak > 0 }
        val streakMessage = when {
            activeStreaks >= 5 -> "You're on fire with $activeStreaks active streaks! 🔥"
            activeStreaks >= 3 -> "Great job maintaining $activeStreaks streaks! 💪"
            activeStreaks >= 1 -> "You have $activeStreaks active streak${if (activeStreaks > 1) "s" else ""}. Keep going!"
            else -> "Ready to start building some habits today?"
        }

        return """
            $timeGreeting, $userName! 👋
            
            $streakMessage
            
            How can I help you today? I can assist with:
            
            📅 Daily schedules & routines
            🥗 Diet & nutrition plans
            💪 Exercise & workout routines
            😴 Sleep optimization
            🧘 Meditation & mindfulness
            📚 Study techniques
            💧 Hydration tips
            🎯 Motivation & goal setting
            
            Just ask me anything! 😊
        """.trimIndent()
    }

    private fun generateDetailedScheduleResponse(userProfile: UserProfile?): String {
        val wakeTime = userProfile?.wakeUpTime ?: "06:00"
        val sleepTime = userProfile?.sleepTime ?: "22:00"

        return """
            📅 **YOUR PERSONALIZED DAILY SCHEDULE**
            
            ═══════════════════════════════
            🌅 **MORNING ROUTINE** (${wakeTime} - 9:00 AM)
            ═══════════════════════════════
            
            ⏰ ${wakeTime} - Wake up & hydrate
               └─ Drink 1 glass of water immediately
               
            ⏰ ${addMinutes(wakeTime, 10)} - Morning stretches
               └─ 5-10 min gentle stretching
               
            ⏰ ${addMinutes(wakeTime, 20)} - Meditation
               └─ 10-15 min mindfulness practice
               
            ⏰ ${addMinutes(wakeTime, 35)} - Exercise
               └─ 30-45 min workout session
               
            ⏰ ${addMinutes(wakeTime, 80)} - Shower & freshen up
            
            ⏰ ${addMinutes(wakeTime, 100)} - Healthy breakfast
               └─ Protein + complex carbs + fruits
            
            ═══════════════════════════════
            ☀️ **PRODUCTIVE HOURS** (9:00 AM - 6:00 PM)
            ═══════════════════════════════
            
            ⏰ 9:00 AM - Deep work block #1
               └─ Most important tasks
               
            ⏰ 10:30 AM - Short break (10 min)
               └─ Walk + water + snack
               
            ⏰ 10:40 AM - Deep work block #2
            
            ⏰ 12:30 PM - Lunch break
               └─ Healthy meal + light walk
               
            ⏰ 2:00 PM - Deep work block #3
            
            ⏰ 3:30 PM - Afternoon break
               └─ Healthy snack + stretch
               
            ⏰ 4:00 PM - Deep work block #4
            
            ═══════════════════════════════
            🌙 **EVENING ROUTINE** (6:00 PM - ${sleepTime})
            ═══════════════════════════════
            
            ⏰ 6:00 PM - End work, light activity
            ⏰ 7:00 PM - Dinner
            ⏰ 8:00 PM - Self-care & hobbies
            ⏰ 9:00 PM - Wind down routine
            ⏰ ${subtractMinutes(sleepTime, 30)} - Reading
            ⏰ ${subtractMinutes(sleepTime, 10)} - Meditation/Gratitude
            ⏰ ${sleepTime} - Sleep 😴
            
            ═══════════════════════════════
            💡 **PRO TIPS**
            ═══════════════════════════════
            
            ✓ Drink water every 2 hours
            ✓ Take 5-min breaks every hour
            ✓ No screens 1 hour before bed
            ✓ Keep consistent sleep times
            
            Would you like me to customize any part of this schedule? 🎯
        """.trimIndent()
    }

    private fun generateDetailedDietResponse(userProfile: UserProfile?): String {
        val preference = userProfile?.dietaryPreference ?: "balanced"

        return """
            🥗 **YOUR PERSONALIZED DIET PLAN**
            
            ═══════════════════════════════
            🌅 **BREAKFAST** (7:00 - 8:00 AM)
            ═══════════════════════════════
            
            **Option 1: Power Bowl**
            • Oatmeal with banana, berries & almonds
            • Green tea or black coffee
            • Calories: ~400
            
            **Option 2: Protein Start**
            • 2 eggs (any style) + whole grain toast
            • Avocado + tomatoes
            • Fresh orange juice
            • Calories: ~450
            
            **Option 3: Quick & Healthy**
            • Greek yogurt parfait
            • Mixed berries + granola + honey
            • Calories: ~350
            
            ═══════════════════════════════
            🍎 **MID-MORNING SNACK** (10:00 AM)
            ═══════════════════════════════
            
            Choose one:
            • Apple + 10 almonds (180 cal)
            • Banana + peanut butter (200 cal)
            • Handful of mixed nuts (170 cal)
            • Carrot sticks + hummus (150 cal)
            
            + Drink: Green tea or water
            
            ═══════════════════════════════
            🍽️ **LUNCH** (12:30 - 1:30 PM)
            ═══════════════════════════════
            
            **Balanced Plate Formula:**
            • 1/4 plate: Lean protein (chicken/fish/tofu)
            • 1/4 plate: Complex carbs (brown rice/quinoa)
            • 1/2 plate: Colorful vegetables
            
            **Sample Meals:**
            1. Grilled chicken salad with quinoa
            2. Fish with steamed veggies & brown rice
            3. Buddha bowl with tofu
            
            + 2 glasses of water
            
            ═══════════════════════════════
            🥜 **AFTERNOON SNACK** (3:30 PM)
            ═══════════════════════════════
            
            Choose one:
            • Protein bar (150-200 cal)
            • Greek yogurt (120 cal)
            • Dark chocolate (2 squares) + nuts
            • Smoothie (banana + berries + almond milk)
            
            ═══════════════════════════════
            🥘 **DINNER** (7:00 - 8:00 PM)
            ═══════════════════════════════
            
            **Keep it light!**
            • Grilled fish/chicken (palm-sized)
            • Large portion of vegetables
            • Light soup or salad
            
            ⚠️ Avoid: Heavy carbs, fried foods
            
            ═══════════════════════════════
            🌙 **EVENING** (If hungry)
            ═══════════════════════════════
            
            • Warm milk with turmeric
            • Chamomile tea
            • Small handful of nuts
            
            ═══════════════════════════════
            💧 **HYDRATION GUIDE**
            ═══════════════════════════════
            
            Daily target: 8-10 glasses (2-2.5L)
            
            ⏰ Morning: 2 glasses
            ⏰ Before lunch: 2 glasses
            ⏰ Afternoon: 2 glasses
            ⏰ Evening: 2 glasses
            
            ═══════════════════════════════
            ⚠️ **FOODS TO AVOID**
            ═══════════════════════════════
            
            ❌ Sugary drinks & sodas
            ❌ Processed foods
            ❌ Excessive caffeine (after 2 PM)
            ❌ Late-night heavy meals
            ❌ Alcohol (limit to weekends)
            
            Would you like specific recipes or meal prep tips? 🍳
        """.trimIndent()
    }

    private fun generateDetailedExerciseResponse(): String {
        return """
            💪 **YOUR COMPLETE WORKOUT PLAN**
            
            ═══════════════════════════════
            📅 **WEEKLY SCHEDULE**
            ═══════════════════════════════
            
            ┌─────────────────────────────┐
            │ MON │ Upper Body    │ 45min │
            │ TUE │ Cardio/HIIT   │ 30min │
            │ WED │ Lower Body    │ 45min │
            │ THU │ Active Rest   │ 20min │
            │ FRI │ Full Body     │ 45min │
            │ SAT │ Fun Activity  │ 60min │
            │ SUN │ Rest          │   -   │
            └─────────────────────────────┘
            
            ═══════════════════════════════
            🔵 **MONDAY: UPPER BODY**
            ═══════════════════════════════
            
            🔥 Warm-up (5 min)
            • Jumping jacks - 1 min
            • Arm circles - 1 min
            • High knees - 1 min
            • Shadow boxing - 2 min
            
            💪 Workout (35 min)
            
            1️⃣ Push-ups
               └─ 3 sets × 12-15 reps
               └─ Rest: 60 sec
            
            2️⃣ Dumbbell Rows
               └─ 3 sets × 10 reps each arm
               └─ Rest: 60 sec
            
            3️⃣ Shoulder Press
               └─ 3 sets × 10 reps
               └─ Rest: 60 sec
            
            4️⃣ Bicep Curls
               └─ 3 sets × 12 reps
               └─ Rest: 45 sec
            
            5️⃣ Tricep Dips
               └─ 3 sets × 12 reps
               └─ Rest: 45 sec
            
            6️⃣ Plank
               └─ 3 sets × 30-45 sec
               └─ Rest: 30 sec
            
            🧘 Cool-down (5 min)
            • Shoulder stretch
            • Tricep stretch
            • Chest opener
            • Deep breathing
            
            ═══════════════════════════════
            🔴 **TUESDAY: CARDIO/HIIT**
            ═══════════════════════════════
            
            **Option 1: HIIT (20 min)**
            • 30 sec work / 30 sec rest × 20
            • Exercises: Burpees, mountain climbers,
              jump squats, high knees
            
            **Option 2: Steady Cardio (30 min)**
            • Jogging / brisk walking
            • Cycling
            • Swimming
            • Jump rope
            
            ═══════════════════════════════
            🟢 **WEDNESDAY: LOWER BODY**
            ═══════════════════════════════
            
            1️⃣ Squats - 4 × 15
            2️⃣ Lunges - 3 × 12 each leg
            3️⃣ Deadlifts - 3 × 10
            4️⃣ Calf Raises - 3 × 20
            5️⃣ Glute Bridges - 3 × 15
            6️⃣ Wall Sit - 3 × 45 sec
            
            ═══════════════════════════════
            🟡 **THURSDAY: ACTIVE RECOVERY**
            ═══════════════════════════════
            
            • 20-30 min yoga flow
            • Light stretching
            • Foam rolling
            • Walking
            
            ═══════════════════════════════
            🟣 **FRIDAY: FULL BODY**
            ═══════════════════════════════
            
            Circuit (3 rounds):
            1. Burpees × 10
            2. Push-ups × 15
            3. Squats × 20
            4. Mountain Climbers × 20
            5. Plank × 45 sec
            6. Jumping Jacks × 30
            
            Rest 2 min between rounds
            
            ═══════════════════════════════
            ⚡ **PRO TIPS**
            ═══════════════════════════════
            
            ✓ Always warm up before workout
            ✓ Stay hydrated (drink during workout)
            ✓ Listen to your body
            ✓ Progress gradually
            ✓ Get 7-8 hours sleep for recovery
            ✓ Eat protein within 30 min post-workout
            
            Want me to modify any exercises for your fitness level? 🎯
        """.trimIndent()
    }

    private fun generateDetailedSleepResponse(userProfile: UserProfile?): String {
        return """
            😴 **YOUR COMPLETE SLEEP OPTIMIZATION GUIDE**
            
            ═══════════════════════════════
            🌙 **THE IDEAL SLEEP SCHEDULE**
            ═══════════════════════════════
            
            🛏️ Bedtime: 10:00 PM - 10:30 PM
            ⏰ Wake up: 6:00 AM - 6:30 AM
            💤 Total: 7.5 - 8 hours
            
            ═══════════════════════════════
            🌆 **EVENING WIND-DOWN ROUTINE**
            ═══════════════════════════════
            
            **2 Hours Before Bed (8:00 PM)**
            □ Dim the lights in your home
            □ Stop all work activities
            □ No intense exercise
            □ Light dinner (if not eaten)
            
            **1 Hour Before Bed (9:00 PM)**
            □ Put away all screens 📵
            □ Take a warm shower/bath
            □ Change into comfortable clothes
            □ Prepare tomorrow's essentials
            
            **30 Min Before Bed (9:30 PM)**
            □ Herbal tea (chamomile/lavender)
            □ Light reading (physical book)
            □ Gentle stretching
            □ Gratitude journaling
            
            **15 Min Before Bed (9:45 PM)**
            □ Meditation or deep breathing
            □ Set room to cool temperature
            □ Complete darkness
            □ White noise if needed
            
            ═══════════════════════════════
            🏠 **BEDROOM OPTIMIZATION**
            ═══════════════════════════════
            
            🌡️ **Temperature:** 65-68°F (18-20°C)
            
            🌑 **Darkness:** 
            • Use blackout curtains
            • Cover LED lights
            • No phone in bedroom
            
            🔇 **Quiet:**
            • Use earplugs if needed
            • White noise machine
            • Fan for ambient sound
            
            🛏️ **Comfort:**
            • Quality mattress & pillows
            • Clean, fresh sheets weekly
            • Comfortable sleepwear
            
            ═══════════════════════════════
            ☕ **SLEEP KILLERS TO AVOID**
            ═══════════════════════════════
            
            ❌ Caffeine after 2:00 PM
            ❌ Alcohol before bed
            ❌ Large meals late at night
            ❌ Screen time before bed
            ❌ Intense exercise evening
            ❌ Stressful conversations
            ❌ Work emails at night
            
            ═══════════════════════════════
            🌅 **MORNING WAKE-UP ROUTINE**
            ═══════════════════════════════
            
            1. Wake at same time daily
            2. Get sunlight within 30 min
            3. Drink water immediately
            4. Light stretching
            5. Avoid snoozing!
            
            ═══════════════════════════════
            💊 **NATURAL SLEEP AIDS**
            ═══════════════════════════════
            
            ✓ Chamomile tea
            ✓ Warm milk
            ✓ Lavender aromatherapy
            ✓ Magnesium-rich foods
            ✓ Tart cherry juice
            
            ═══════════════════════════════
            📊 **SLEEP QUALITY CHECKLIST**
            ═══════════════════════════════
            
            □ Fall asleep within 20 min
            □ Sleep through the night
            □ Wake up refreshed
            □ No daytime sleepiness
            □ Consistent schedule
            
            If you're struggling with any of these, let me know and I can provide more specific tips! 💤
        """.trimIndent()
    }

    private fun generateDetailedMeditationResponse(): String {
        return """
            🧘 **COMPLETE MEDITATION & MINDFULNESS GUIDE**
            
            ═══════════════════════════════
            🌟 **BEGINNER'S MEDITATION**
            ═══════════════════════════════
            
            **Start with just 5 minutes!**
            
            📍 **Setup:**
            1. Find a quiet spot
            2. Sit comfortably (chair/cushion)
            3. Set timer for 5 minutes
            4. Close eyes gently
            
            🌬️ **Basic Breathing Meditation:**
            
            1. Breathe naturally
            2. Focus on your breath
            3. Notice the inhale... exhale...
            4. When mind wanders (it will!)
               → Gently return to breath
            5. No judgment, just observe
            
            ═══════════════════════════════
            📅 **DAILY MEDITATION SCHEDULE**
            ═══════════════════════════════
            
            🌅 **Morning (5-10 min)**
            Purpose: Set intentions for the day
            Best for: Energy & focus
            
            ☀️ **Midday (5 min)**
            Purpose: Reset & de-stress
            Best for: Clarity & calm
            
            🌙 **Evening (10-15 min)**
            Purpose: Release the day
            Best for: Relaxation & sleep
            
            ═══════════════════════════════
            🎯 **MEDITATION TECHNIQUES**
            ═══════════════════════════════
            
            **1️⃣ Body Scan (10 min)**
            ┌────────────────────────────┐
            │ Focus attention on each    │
            │ body part from toes to     │
            │ head, releasing tension    │
            └────────────────────────────┘
            Best for: Physical relaxation
            
            **2️⃣ Loving-Kindness (10 min)**
            ┌────────────────────────────┐
            │ Send love & positive       │
            │ wishes to yourself, loved  │
            │ ones, and all beings       │
            └────────────────────────────┘
            Best for: Emotional healing
            
            **3️⃣ Mindful Walking (15 min)**
            ┌────────────────────────────┐
            │ Walk slowly, focusing on   │
            │ each step and sensation    │
            └────────────────────────────┘
            Best for: Active meditation
            
            **4️⃣ Gratitude Meditation (5 min)**
            ┌────────────────────────────┐
            │ Reflect on 3-5 things      │
            │ you're grateful for        │
            └────────────────────────────┘
            Best for: Positive mindset
            
            **5️⃣ Box Breathing (5 min)**
            ┌────────────────────────────┐
            │ Inhale 4 sec → Hold 4 sec  │
            │ Exhale 4 sec → Hold 4 sec  │
            │ Repeat                     │
            └────────────────────────────┘
            Best for: Anxiety relief
            
            ═══════════════════════════════
            🚀 **PROGRESSION PLAN**
            ═══════════════════════════════
            
            Week 1-2: 5 min/day
            Week 3-4: 10 min/day
            Week 5-6: 15 min/day
            Week 7+: 20 min/day
            
            ═══════════════════════════════
            💡 **TIPS FOR SUCCESS**
            ═══════════════════════════════
            
            ✓ Same time, same place daily
            ✓ Start small (5 min)
            ✓ Don't judge wandering mind
            ✓ Use guided apps initially
            ✓ Be patient with yourself
            ✓ Track your practice
            
            ═══════════════════════════════
            😰 **FOR STRESS & ANXIETY**
            ═══════════════════════════════
            
            **Quick Relief (2 min):**
            1. Stop what you're doing
            2. Take 5 deep breaths
            3. Name 5 things you can see
            4. Name 4 things you can hear
            5. Name 3 things you can feel
            
            This is called the 5-4-3 grounding technique!
            
            Would you like me to guide you through a specific meditation? 🙏
        """.trimIndent()
    }

    private fun generateDetailedHydrationResponse(): String {
        return """
            💧 **YOUR COMPLETE HYDRATION GUIDE**
            
            ═══════════════════════════════
            📊 **DAILY WATER INTAKE**
            ═══════════════════════════════
            
            **General Guideline:**
            • Women: 2.7L (91 oz) daily
            • Men: 3.7L (125 oz) daily
            
            **Simple Rule:**
            8 glasses × 8 oz = 64 oz minimum
            
            **Personalized Calculation:**
            Your weight (lbs) ÷ 2 = oz of water
            Example: 150 lbs → 75 oz daily
            
            ═══════════════════════════════
            ⏰ **HYDRATION SCHEDULE**
            ═══════════════════════════════
            
            ⏰ 6:00 AM │ Wake up    │ 💧💧 (16 oz)
            ⏰ 8:00 AM │ Breakfast  │ 💧 (8 oz)
            ⏰ 10:00AM │ Mid-morn   │ 💧 (8 oz)
            ⏰ 12:00PM │ Pre-lunch  │ 💧 (8 oz)
            ⏰ 2:00 PM │ Afternoon  │ 💧 (8 oz)
            ⏰ 4:00 PM │ Mid-after  │ 💧 (8 oz)
            ⏰ 6:00 PM │ Pre-dinner │ 💧 (8 oz)
            ⏰ 8:00 PM │ Evening    │ 💧 (8 oz)
            
            Total: 72 oz ✓
            
            ═══════════════════════════════
            🍉 **HYDRATING FOODS**
            ═══════════════════════════════
            
            🥒 Cucumber - 96% water
            🥬 Lettuce - 96% water
            🍉 Watermelon - 92% water
            🍓 Strawberries - 91% water
            🍊 Oranges - 87% water
            🍑 Peaches - 89% water
            
            ═══════════════════════════════
            ✅ **HYDRATION HACKS**
            ═══════════════════════════════
            
            1️⃣ **Morning Ritual**
            └─ 16 oz water right after waking
            
            2️⃣ **Bottle Buddy**
            └─ Carry water bottle everywhere
            
            3️⃣ **Flavor Boost**
            └─ Add lemon, cucumber, or mint
            
            4️⃣ **App Reminders**
            └─ Set hourly water reminders
            
            5️⃣ **Before Meals**
            └─ Drink 8 oz before eating
            
            6️⃣ **Track Progress**
            └─ Mark bottles/glasses drank
            
            ═══════════════════════════════
            ⚠️ **DEHYDRATION SIGNS**
            ═══════════════════════════════
            
            🟡 **Mild:**
            • Dark yellow urine
            • Thirst
            • Dry mouth
            
            🟠 **Moderate:**
            • Headache
            • Fatigue
            • Dizziness
            
            🔴 **Severe:**
            • Rapid heartbeat
            • Confusion
            • Very dark urine
            
            ═══════════════════════════════
            🎯 **URINE COLOR CHART**
            ═══════════════════════════════
            
            💛 Pale yellow = Well hydrated ✓
            💛 Light yellow = Hydrated ✓
            🟡 Yellow = Drink more water
            🟠 Dark yellow = Dehydrated!
            🟤 Amber = Severely dehydrated!
            
            ═══════════════════════════════
            ❌ **WHAT DEHYDRATES YOU**
            ═══════════════════════════════
            
            • Caffeine (moderate)
            • Alcohol
            • Salty foods
            • Sugary drinks
            • Hot weather
            • Exercise
            
            For every caffeinated drink, add an extra glass of water!
            
            Set your water reminders and let's stay hydrated! 💪
        """.trimIndent()
    }

    private fun generateDetailedStudyResponse(): String {
        return """
            📚 **ULTIMATE STUDY & FOCUS GUIDE**
            
            ═══════════════════════════════
            🧠 **THE POMODORO TECHNIQUE**
            ═══════════════════════════════
            
            ┌────────────────────────────┐
            │  📖 STUDY     │  25 min    │
            │  ☕ BREAK     │   5 min    │
            │  📖 STUDY     │  25 min    │
            │  ☕ BREAK     │   5 min    │
            │  📖 STUDY     │  25 min    │
            │  ☕ BREAK     │   5 min    │
            │  📖 STUDY     │  25 min    │
            │  🎉 LONG BREAK│  15-30 min │
            └────────────────────────────┘
            
            **Why it works:**
            • Prevents burnout
            • Improves focus
            • Creates urgency
            • Tracks progress
            
            ═══════════════════════════════
            🔄 **ACTIVE RECALL METHOD**
            ═══════════════════════════════
            
            **Instead of:** Re-reading notes ❌
            **Do this:** Test yourself ✓
            
            1. Read a section once
            2. Close the book
            3. Write down everything you remember
            4. Check what you missed
            5. Focus on missed parts
            6. Repeat!
            
            **This is 3x more effective than re-reading!**
            
            ═══════════════════════════════
            📅 **SPACED REPETITION**
            ═══════════════════════════════
            
            Review schedule for retention:
            
            Day 1: Learn new material
            Day 2: First review ← 🔴 Critical!
            Day 4: Second review
            Day 7: Third review
            Day 14: Fourth review
            Day 30: Fifth review
            
            After this, it's in long-term memory!
            
            ═══════════════════════════════
            👨‍🏫 **FEYNMAN TECHNIQUE**
            ═══════════════════════════════
            
            1. Choose a concept
            2. Teach it to a 5-year-old
            3. Identify gaps in your explanation
            4. Review and simplify
            5. Repeat until crystal clear
            
            *"If you can't explain it simply, you don't understand it well enough."* - Einstein
            
            ═══════════════════════════════
            🎯 **OPTIMAL STUDY ENVIRONMENT**
            ═══════════════════════════════
            
            ✅ **DO:**
            • Clean, organized desk
            • Good lighting
            • Comfortable temperature
            • Water bottle nearby
            • All materials ready
            • Phone on airplane mode ✈️
            
            ❌ **AVOID:**
            • Bed (you'll get sleepy)
            • TV in background
            • Social media
            • Noisy environments
            • Clutter
            
            ═══════════════════════════════
            ⏰ **BEST STUDY TIMES**
            ═══════════════════════════════
            
            🌅 **Morning (9-11 AM)**
            Best for: Analytical tasks, math, logic
            Peak alertness!
            
            🌞 **Afternoon (3-5 PM)**
            Best for: Creative work, writing
            Second peak!
            
            🌙 **Avoid: Late night cramming**
            Your brain consolidates during sleep!
            
            ═══════════════════════════════
            🧠 **MEMORY BOOSTERS**
            ═══════════════════════════════
            
            💤 **Sleep:** 7-9 hours (non-negotiable!)
            🏃 **Exercise:** Increases BDNF
            💧 **Hydration:** Brain is 75% water
            🥦 **Nutrition:** Omega-3, berries, nuts
            🧘 **Meditation:** Improves focus
            
            ═══════════════════════════════
            📝 **BEFORE EXAMS**
            ═══════════════════════════════
            
            ❌ DON'T:
            • Cram all night
            • Skip sleep
            • Panic review
            
            ✅ DO:
            • Light review only
            • Get 8 hours sleep
            • Healthy breakfast
            • Arrive early
            • Stay calm & confident
            
            You've got this! 💪📚
        """.trimIndent()
    }

    private fun generatePersonalizedMotivationResponse(
        userName: String,
        streaks: Map<String, StreakInfo>
    ): String {
        val activeStreaks = streaks.filter { it.value.currentStreak > 0 }
        val bestStreak = streaks.maxByOrNull { it.value.currentStreak }

        val streakSection = if (activeStreaks.isNotEmpty()) {
            val streakList = activeStreaks.entries.joinToString("\n") { (name, info) ->
                val category = try { Category.valueOf(name) } catch (e: Exception) { null }
                "   🔥 ${category?.displayName ?: name}: ${info.currentStreak} days"
            }
            """
            ═══════════════════════════════
            🏆 **YOUR CURRENT STREAKS**
            ═══════════════════════════════
            
$streakList
            
            That's AMAZING, $userName! 💪
            """
        } else {
            """
            ═══════════════════════════════
            🚀 **TIME TO START!**
            ═══════════════════════════════
            
            Hey $userName, today is the perfect day
            to start your first streak!
            
            Pick ONE habit and commit to it today.
            """
        }

        val quotes = listOf(
            Triple("The secret of getting ahead is getting started.", "Mark Twain", "🚀"),
            Triple("It does not matter how slowly you go as long as you do not stop.", "Confucius", "🐢"),
            Triple("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "💪"),
            Triple("The only bad workout is the one that didn't happen.", "Unknown", "🏋️"),
            Triple("Small daily improvements are the key to staggering long-term results.", "Robin Sharma", "📈"),
            Triple("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar", "⭐"),
            Triple("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln", "🎯"),
            Triple("The pain of discipline is far less than the pain of regret.", "Sarah Bombell", "💎")
        )

        val (quote, author, emoji) = quotes.random()

        return """
            💪 **MOTIVATION BOOST FOR $userName!**
            
            $streakSection
            
            ═══════════════════════════════
            $emoji **TODAY'S INSPIRATION**
            ═══════════════════════════════
            
            "$quote"
            
            — $author
            
            ═══════════════════════════════
            🧠 **REMEMBER THIS:**
            ═══════════════════════════════
            
            ✨ Every expert was once a beginner
            ✨ Progress, not perfection
            ✨ One day or day one - you decide
            ✨ Your only competition is yesterday's you
            ✨ The best time to start was yesterday,
               the second best time is NOW
            
            ═══════════════════════════════
            🎯 **YOUR MISSION TODAY:**
            ═══════════════════════════════
            
            1. Pick your most important habit
            2. Do it for just 5 minutes
            3. That's it - just start!
            
            The hardest part is starting.
            Once you begin, momentum takes over.
            
            ═══════════════════════════════
            🌟 **I BELIEVE IN YOU!**
            ═══════════════════════════════
            
            $userName, you have everything it takes.
            Today is YOUR day to shine! ✨
            
            Now go crush those habits! 💪🔥
        """.trimIndent()
    }

    private fun generateProgressReviewResponse(
        userName: String,
        streaks: Map<String, StreakInfo>
    ): String {
        val totalActive = streaks.values.count { it.currentStreak > 0 }
        val totalCategories = streaks.size
        val bestStreak = streaks.maxByOrNull { it.value.currentStreak }
        val longestEver = streaks.maxByOrNull { it.value.longestStreak }

        val streakDetails = streaks.entries.joinToString("\n") { (name, info) ->
            val category = try { Category.valueOf(name) } catch (e: Exception) { null }
            val status = if (info.currentStreak > 0) "🔥 ${info.currentStreak} days" else "⚪ Not active"
            "│ ${(category?.displayName ?: name).padEnd(12)} │ $status │"
        }

        val rating = when {
            totalActive >= 7 -> "🏆 LEGENDARY"
            totalActive >= 5 -> "⭐ EXCELLENT"
            totalActive >= 3 -> "💪 GOOD"
            totalActive >= 1 -> "🌱 GROWING"
            else -> "🚀 READY TO START"
        }

        return """
            📊 **$userName'S PROGRESS REPORT**
            
            ═══════════════════════════════
            🎯 **OVERVIEW**
            ═══════════════════════════════
            
            Active Streaks: $totalActive / $totalCategories
            Status: $rating
            
            ═══════════════════════════════
            📈 **STREAK DETAILS**
            ═══════════════════════════════
            
            ┌──────────────┬─────────────┐
$streakDetails
            └──────────────┴─────────────┘
            
            ═══════════════════════════════
            🏅 **ACHIEVEMENTS**
            ═══════════════════════════════
            
            🔥 Best Current Streak:
               ${bestStreak?.let {
            val cat = try { Category.valueOf(it.key) } catch (e: Exception) { null }
            "${cat?.displayName ?: it.key} - ${it.value.currentStreak} days"
        } ?: "None yet - start today!"}
            
            👑 Longest Ever Streak:
               ${longestEver?.let {
            val cat = try { Category.valueOf(it.key) } catch (e: Exception) { null }
            "${cat?.displayName ?: it.key} - ${it.value.longestStreak} days"
        } ?: "None yet - start today!"}
            
            ═══════════════════════════════
            💡 **RECOMMENDATIONS**
            ═══════════════════════════════
            
            ${generateRecommendations(streaks)}
            
            Keep pushing forward, $userName! 🚀
        """.trimIndent()
    }

    private fun generateRecommendations(streaks: Map<String, StreakInfo>): String {
        val inactive = streaks.filter { it.value.currentStreak == 0 }
        val active = streaks.filter { it.value.currentStreak > 0 }

        return buildString {
            if (inactive.isNotEmpty()) {
                val suggestion = inactive.entries.random()
                val cat = try { Category.valueOf(suggestion.key) } catch (e: Exception) { null }
                appendLine("🎯 Try focusing on ${cat?.displayName ?: suggestion.key} today!")
            }

            if (active.isNotEmpty()) {
                appendLine("💪 Great job on your active streaks!")
                appendLine("🔥 Don't break your momentum!")
            }

            appendLine("\n📌 Remember: Consistency beats intensity!")
        }
    }

    private fun generateYogaResponse(): String {
        return """
            🧘 **YOUR YOGA & STRETCHING GUIDE**
            
            ═══════════════════════════════
            🌅 **MORNING YOGA (15 min)**
            ═══════════════════════════════
            
            1️⃣ Cat-Cow Stretch (1 min)
            2️⃣ Downward Dog (30 sec)
            3️⃣ Forward Fold (30 sec)
            4️⃣ Warrior I - both sides (1 min)
            5️⃣ Warrior II - both sides (1 min)
            6️⃣ Triangle Pose (1 min)
            7️⃣ Tree Pose (1 min)
            8️⃣ Child's Pose (1 min)
            9️⃣ Seated Twist (1 min)
            🔟 Savasana (2 min)
            
            ═══════════════════════════════
            🌙 **EVENING RELAXATION (10 min)**
            ═══════════════════════════════
            
            1️⃣ Neck rolls (1 min)
            2️⃣ Shoulder shrugs (1 min)
            3️⃣ Seated forward fold (2 min)
            4️⃣ Supine twist (2 min each side)
            5️⃣ Legs up the wall (3 min)
            
            ═══════════════════════════════
            💡 **YOGA TIPS**
            ═══════════════════════════════
            
            • Never force a stretch
            • Breathe deeply throughout
            • Listen to your body
            • Practice on an empty stomach
            • Use a yoga mat for grip
            • Wear comfortable clothes
            
            Namaste! 🙏
        """.trimIndent()
    }

    private fun generateSelfCareResponse(): String {
        return """
            💝 **YOUR SELF-CARE GUIDE**
            
            ═══════════════════════════════
            🛁 **PHYSICAL SELF-CARE**
            ═══════════════════════════════
            
            • Take relaxing baths/showers
            • Get enough sleep (7-9 hours)
            • Eat nutritious meals
            • Stay hydrated
            • Exercise regularly
            • Get fresh air & sunshine
            
            ═══════════════════════════════
            🧠 **MENTAL SELF-CARE**
            ═══════════════════════════════
            
            • Practice meditation
            • Journal your thoughts
            • Read inspiring books
            • Learn something new
            • Take breaks from screens
            • Set healthy boundaries
            
            ═══════════════════════════════
            💜 **EMOTIONAL SELF-CARE**
            ═══════════════════════════════
            
            • Express your feelings
            • Practice self-compassion
            • Connect with loved ones
            • Do activities you enjoy
            • Say no when needed
            • Celebrate small wins
            
            ═══════════════════════════════
            🌟 **QUICK SELF-CARE IDEAS**
            ═══════════════════════════════
            
            5 Minutes:
            • Deep breathing
            • Stretch
            • Drink water
            • Step outside
            
            15 Minutes:
            • Short walk
            • Meditation
            • Face mask
            • Call a friend
            
            30+ Minutes:
            • Bubble bath
            • Read a book
            • Cook a healthy meal
            • Creative hobby
            
            You deserve to take care of yourself! 💕
        """.trimIndent()
    }

    private fun generateGoalSettingResponse(streaks: Map<String, StreakInfo>): String {
        return """
            🎯 **GOAL SETTING FRAMEWORK**
            
            ═══════════════════════════════
            📝 **SMART GOALS**
            ═══════════════════════════════
            
            S - Specific (What exactly?)
            M - Measurable (How much?)
            A - Achievable (Is it realistic?)
            R - Relevant (Why does it matter?)
            T - Time-bound (By when?)
            
            ═══════════════════════════════
            🎯 **EXAMPLE GOALS**
            ═══════════════════════════════
            
            ❌ Bad: "I want to exercise more"
            ✅ Good: "I will exercise 30 min, 
                     5 days/week for 1 month"
            
            ❌ Bad: "I want to eat healthy"
            ✅ Good: "I will eat 5 servings of 
                     vegetables daily for 2 weeks"
            
            ═══════════════════════════════
            📈 **YOUR SUGGESTED GOALS**
            ═══════════════════════════════
            
            Based on your current progress:
            
            Week 1: Complete 3 habits daily
            Week 2: Complete 5 habits daily
            Week 3: Complete 6 habits daily
            Week 4: Complete all 8 habits daily!
            
            ═══════════════════════════════
            💡 **TIPS FOR SUCCESS**
            ═══════════════════════════════
            
            • Start small
            • Track daily
            • Celebrate progress
            • Adjust as needed
            • Don't give up!
            
            What goal would you like to set? 🚀
        """.trimIndent()
    }

    private fun generateRandomTipsResponse(): String {
        val tips = listOf(
            "🌅 **Morning Tip:** Start your day with a glass of water and 5 minutes of stretching!",
            "💪 **Fitness Tip:** Take the stairs instead of the elevator - small changes add up!",
            "🧠 **Productivity Tip:** Do your hardest task first thing in the morning when willpower is highest!",
            "😴 **Sleep Tip:** Put your phone in another room at night - you'll sleep better!",
            "🥗 **Nutrition Tip:** Prep your meals on Sunday to eat healthy all week!",
            "🧘 **Mindfulness Tip:** Take 3 deep breaths before responding to stressful situations!",
            "💧 **Hydration Tip:** Set a reminder to drink water every hour!",
            "📚 **Learning Tip:** Teach what you learn to someone else - you'll remember it better!"
        )

        return """
            💡 **DAILY TIPS & TRICKS**
            
            ${tips.shuffled().take(4).joinToString("\n\n")}
            
            ═══════════════════════════════
            
            Want more tips on a specific topic? Just ask! 🎯
        """.trimIndent()
    }

    private fun generateThankYouResponse(): String {
        val responses = listOf(
            "You're welcome! 😊 Happy to help anytime!",
            "My pleasure! 🌟 Keep up the great work!",
            "Anytime! 💪 You've got this!",
            "Glad I could help! 🎯 Remember, I'm always here for you!"
        )
        return responses.random()
    }

    private fun generateGoodbyeResponse(userName: String): String {
        return """
            👋 Goodbye, $userName!
            
            Remember:
            ✨ Small steps lead to big changes
            ✨ Consistency is key
            ✨ You're doing amazing!
            
            See you next time! Take care and keep building those habits! 💪🔥
        """.trimIndent()
    }

    private fun generateSmartDefaultResponse(
        userMessage: String,
        streaks: Map<String, StreakInfo>
    ): String {
        return """
            I'd be happy to help you with that! 😊
            
            Here are some things I can assist with:
            
            📅 **Schedules & Routines**
            └─ "Create my daily schedule"
            
            🥗 **Diet & Nutrition**
            └─ "Give me a diet plan"
            
            💪 **Fitness & Exercise**
            └─ "Workout routine for beginners"
            
            😴 **Sleep & Rest**
            └─ "How to sleep better"
            
            🧘 **Meditation & Mindfulness**
            └─ "Guide me through meditation"
            
            💧 **Hydration**
            └─ "Hydration tips"
            
            📚 **Study & Productivity**
            └─ "Best study techniques"
            
            🎯 **Motivation**
            └─ "Motivate me"
            
            📊 **Progress Review**
            └─ "How am I doing?"
            
            Just ask me anything from the list above! 🌟
        """.trimIndent()
    }

    // Helper functions
    private fun addMinutes(time: String, minutes: Int): String {
        return try {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val totalMinutes = hour * 60 + minute + minutes
            val newHour = (totalMinutes / 60) % 24
            val newMinute = totalMinutes % 60
            String.format("%02d:%02d", newHour, newMinute)
        } catch (e: Exception) {
            time
        }
    }

    private fun subtractMinutes(time: String, minutes: Int): String {
        return addMinutes(time, -minutes)
    }
}