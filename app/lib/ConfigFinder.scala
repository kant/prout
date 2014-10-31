package lib

import com.madgag.git._
import lib.Config.{RepoConfig, Checkpoint}
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import play.api.Logger

object ConfigFinder {

  private val configFilter: TreeWalk => Boolean = w => w.isSubtree || w.getNameString == ".prout.json"

  /**
   *
   * @return treewalk that only returns prout config files
   */
  def configTreeWalk(c: RevCommit)(implicit reader: ObjectReader): TreeWalk = walk(c.getTree)(configFilter)

  def configIdMapFrom(c: RevCommit)(implicit reader: ObjectReader) = configTreeWalk(c).map { tw =>
    val configPath = tw.slashPrefixedPath
    configPath.reverse.dropWhile(_ != '/').reverse -> tw.getObjectId(0)
  }.toMap

  def config(c: RevCommit)(implicit reader: ObjectReader): RepoConfig = {
    val checkpointsByNameByFolder: Map[String, Set[Checkpoint]] = configIdMapFrom(c).mapValues(Config.readConfigFrom)
    println(s"Book $checkpointsByNameByFolder")
    RepoConfig(checkpointsByNameByFolder)
  }
}