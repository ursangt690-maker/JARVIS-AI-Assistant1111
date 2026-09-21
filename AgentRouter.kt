package com.zoya.assistant.agent

/**
 * Lightweight local agent router. It classifies a request so the model can
 * apply the right specialist behavior without exposing arbitrary tools.
 */
object AgentRouter {
    enum class Agent { GENERAL, RESEARCH, VISION, AUTOMATION, SYSTEM, CODING, PLANNER }

    fun route(text: String): Agent {
        val s = text.lowercase()
        return when {
            listOf("research", "search deeply", "find sources", "अनुसन्धान", "खोज").any { s.contains(it) } -> Agent.RESEARCH
            listOf("image", "photo", "picture", "screenshot", "vision", "फोटो", "तस्बिर").any { s.contains(it) } -> Agent.VISION
            listOf("automate", "automation", "workflow", "routine", "स्वचालित").any { s.contains(it) } -> Agent.AUTOMATION
            listOf("battery", "volume", "wifi", "bluetooth", "settings", "flashlight", "system", "फोन").any { s.contains(it) } -> Agent.SYSTEM
            listOf("code", "coding", "program", "debug", "developer").any { s.contains(it) } -> Agent.CODING
            listOf("plan", "schedule", "steps", "योजना").any { s.contains(it) } -> Agent.PLANNER
            else -> Agent.GENERAL
        }
    }

    fun systemPrefix(agent: Agent): String = when (agent) {
        Agent.RESEARCH -> "Research agent: reason carefully, distinguish facts from uncertainty, and structure findings with sources when tools are available."
        Agent.VISION -> "Vision agent: interpret visual content carefully. Never invent details that are not visible."
        Agent.AUTOMATION -> "Automation agent: break the task into safe, explicit steps. Never perform destructive or sensitive actions without confirmation."
        Agent.SYSTEM -> "System agent: use only supported Android actions and report failures truthfully."
        Agent.CODING -> "Coding agent: inspect assumptions, produce robust code, and identify edge cases."
        Agent.PLANNER -> "Planning agent: create a clear ordered plan with dependencies and checkpoints."
        Agent.GENERAL -> "General JARVIS agent: answer naturally and concisely."
    }
}
