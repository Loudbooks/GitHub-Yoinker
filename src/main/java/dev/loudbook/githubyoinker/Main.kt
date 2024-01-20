package dev.loudbook.githubyoinker

import kotlinx.coroutines.runBlocking

object Main {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val githubYoinker = GitHubYoinker()
        githubYoinker.initiate()
    }
}
