package com.serene.mentor.utils

import com.serene.mentor.models.Difficulty
import com.serene.mentor.models.GDFramework
import com.serene.mentor.models.Topic
import com.serene.mentor.models.TopicCategory

object TopicRepository {

    private val ALL_TOPICS = listOf(

        // ── TECHNOLOGY ────────────────────────────────────────
        Topic(
            title = "Artificial Intelligence: Boon or Bane?",
            category = TopicCategory.TECHNOLOGY,
            difficulty = Difficulty.BEGINNER,
            description = "AI is transforming every sector from healthcare to finance. Discuss its advantages, risks, and ethical considerations.",
            keyPoints = listOf(
                "AI improves efficiency and automates repetitive tasks",
                "Concerns about job displacement and automation",
                "Ethical issues: bias in algorithms, privacy",
                "AI in healthcare, education, and agriculture",
                "The need for regulation and responsible AI"
            ),
            suggestedFramework = GDFramework.PROS_CONS,
            sampleOpener = "Artificial Intelligence is arguably the most transformative technology of the 21st century. While it brings unprecedented efficiency and capabilities, we must carefully consider its societal impact..."
        ),
        Topic(
            title = "Social Media: Its Impact on Society",
            category = TopicCategory.TECHNOLOGY,
            difficulty = Difficulty.BEGINNER,
            description = "Social media has revolutionized communication but also introduced challenges around mental health and misinformation.",
            keyPoints = listOf(
                "Connectivity and global communication benefits",
                "Mental health impact on teenagers",
                "Spread of misinformation and fake news",
                "Digital activism and social movements",
                "Privacy and data security concerns"
            ),
            suggestedFramework = GDFramework.CAUSE_EFFECT,
            sampleOpener = "Social media has fundamentally changed how 4.9 billion people connect and communicate. However, this revolution comes with profound consequences that we need to examine critically..."
        ),
        Topic(
            title = "The Future of Remote Work",
            category = TopicCategory.TECHNOLOGY,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Remote work was accelerated by the pandemic. Explore its long-term viability, productivity impact, and cultural shifts.",
            keyPoints = listOf(
                "Productivity gains and flexibility benefits",
                "Challenges: collaboration, culture, work-life balance",
                "Impact on commercial real estate and cities",
                "Hybrid work models as the future",
                "Digital infrastructure requirements"
            ),
            suggestedFramework = GDFramework.PREP,
            sampleOpener = "The pandemic fundamentally disrupted the traditional workplace, and what started as a crisis response has evolved into a permanent paradigm shift in how we define work..."
        ),
        Topic(
            title = "Cryptocurrency: The Currency of the Future?",
            category = TopicCategory.TECHNOLOGY,
            difficulty = Difficulty.ADVANCED,
            description = "Cryptocurrencies challenge traditional finance with decentralization, but face volatility, regulation, and scalability issues.",
            keyPoints = listOf(
                "Decentralization and financial inclusion",
                "Volatility and speculation risks",
                "Regulatory challenges globally",
                "Environmental impact of blockchain mining",
                "Central Bank Digital Currencies (CBDCs)"
            ),
            suggestedFramework = GDFramework.THREE_POINT,
            sampleOpener = "Bitcoin's rise from a whitepaper to a $1 trillion asset class represents either the democratization of finance or the greatest speculative bubble in history — and that ambiguity is precisely what we must debate today..."
        ),

        // ── ECONOMY ───────────────────────────────────────────
        Topic(
            title = "Should the Minimum Wage Be Increased?",
            category = TopicCategory.ECONOMY,
            difficulty = Difficulty.BEGINNER,
            description = "The debate around minimum wage involves worker welfare, business costs, employment rates, and economic growth.",
            keyPoints = listOf(
                "Living wage vs. market-determined wages",
                "Impact on small businesses and employment",
                "Reducing income inequality",
                "Inflationary pressures",
                "International comparisons and case studies"
            ),
            suggestedFramework = GDFramework.PROS_CONS,
            sampleOpener = "The question of minimum wage is fundamentally a question about the kind of society we want to build — one where work always pays enough to live with dignity..."
        ),
        Topic(
            title = "Globalisation: Opportunity or Inequality?",
            category = TopicCategory.ECONOMY,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Globalization has driven unprecedented economic growth but also increased inequality within and between nations.",
            keyPoints = listOf(
                "Trade liberalization and economic growth",
                "Widening wealth gap within nations",
                "Cultural homogenization concerns",
                "Supply chain vulnerabilities (post-COVID lessons)",
                "Winners and losers of globalization"
            ),
            suggestedFramework = GDFramework.CAUSE_EFFECT,
            sampleOpener = "Globalization has lifted over a billion people out of extreme poverty while simultaneously hollowing out middle classes in developed nations — understanding this paradox is central to our discussion today..."
        ),
        Topic(
            title = "Universal Basic Income: A Viable Solution?",
            category = TopicCategory.ECONOMY,
            difficulty = Difficulty.ADVANCED,
            description = "UBI proposes giving every citizen a regular income regardless of employment. Examine feasibility, funding, and social impact.",
            keyPoints = listOf(
                "Automation-driven unemployment as catalyst",
                "Finland and Kenya pilot program results",
                "Funding mechanisms and fiscal sustainability",
                "Effect on work incentives",
                "Poverty elimination vs. inflation risks"
            ),
            suggestedFramework = GDFramework.STAR,
            sampleOpener = "As automation increasingly displaces workers across sectors, Universal Basic Income has shifted from a utopian idea to a serious policy proposal being tested in multiple countries..."
        ),

        // ── SOCIAL ISSUES ─────────────────────────────────────
        Topic(
            title = "Gender Equality in the Workplace",
            category = TopicCategory.SOCIAL_ISSUES,
            difficulty = Difficulty.BEGINNER,
            description = "Despite progress, gender gaps persist in pay, leadership, and opportunity. What more needs to be done?",
            keyPoints = listOf(
                "Gender pay gap statistics and causes",
                "Women in leadership: barriers and progress",
                "Unconscious bias in hiring",
                "Maternity leave and caregiving policies",
                "Role of corporate culture and legislation"
            ),
            suggestedFramework = GDFramework.THREE_POINT,
            sampleOpener = "While women now make up nearly half the global workforce, they hold only 27% of managerial positions and earn on average 20% less than their male counterparts — these numbers demand urgent examination..."
        ),
        Topic(
            title = "The Mental Health Crisis Among Youth",
            category = TopicCategory.SOCIAL_ISSUES,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Rates of anxiety, depression, and suicide among young people are at historic highs. What are the causes and solutions?",
            keyPoints = listOf(
                "Social media and comparison culture",
                "Academic pressure and competitive systems",
                "Stigma around seeking help",
                "Inadequate mental health infrastructure",
                "Role of schools, parents, and policy"
            ),
            suggestedFramework = GDFramework.CAUSE_EFFECT,
            sampleOpener = "One in seven adolescents globally experiences a mental health disorder — yet over 70% of them receive no treatment. This is not merely a health crisis; it is a societal emergency..."
        ),

        // ── ABSTRACT ──────────────────────────────────────────
        Topic(
            title = "Is Failure the Key to Success?",
            category = TopicCategory.ABSTRACT,
            difficulty = Difficulty.BEGINNER,
            description = "Explore how failure contributes to learning, resilience, and eventual success — both at the individual and organizational level.",
            keyPoints = listOf(
                "Learning from mistakes and growth mindset",
                "Famous examples: Edison, Einstein, Rowling",
                "Fear of failure and risk aversion",
                "Cultural attitudes toward failure",
                "Failure in business and innovation"
            ),
            suggestedFramework = GDFramework.PREP,
            sampleOpener = "Thomas Edison failed over a thousand times before inventing the lightbulb. J.K. Rowling was rejected by twelve publishers before Harry Potter. The pattern is undeniable — failure is not the opposite of success, it is the pathway to it..."
        ),
        Topic(
            title = "Tradition vs. Modernity: Finding Balance",
            category = TopicCategory.ABSTRACT,
            difficulty = Difficulty.INTERMEDIATE,
            description = "As societies modernize, the tension between preserving cultural traditions and embracing change intensifies.",
            keyPoints = listOf(
                "Cultural identity and heritage preservation",
                "Modernization and economic development",
                "Generational conflicts and values",
                "Role of globalization in cultural change",
                "Can tradition and progress coexist?"
            ),
            suggestedFramework = GDFramework.THREE_POINT,
            sampleOpener = "Every generation inherits a world shaped by those before them, and must decide what to preserve, what to reform, and what to let go. This negotiation between tradition and modernity defines civilizations..."
        )
    )

    fun getTopicsByCategory(category: TopicCategory, difficulty: Difficulty? = null): List<Topic> {
        return ALL_TOPICS.filter { topic ->
            topic.category == category && (difficulty == null || topic.difficulty == difficulty)
        }
    }

    fun getRandomTopic(category: TopicCategory, difficulty: Difficulty): Topic? {
        return getTopicsByCategory(category, difficulty).randomOrNull()
    }

    fun getAllTopics(): List<Topic> = ALL_TOPICS

    fun getCategories(): List<TopicCategory> = TopicCategory.values().toList()

    fun getDifficulties(): List<Difficulty> = Difficulty.values().toList()
}
