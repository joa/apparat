package apparat.tools

trait ApparatTool {
  def help: String
  def name: String
  def configure(config: ApparatConfiguration)
  def run()
}
