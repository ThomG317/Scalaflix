package model

class Actor (val id: Int, val name: String, val surname: String) {
  def this(name: String, surname: String) {
    this(Math.random().toInt, name, surname)
  }
}
