package model

class Actor (val id: Int, val name: String, val surname: String) {

  def this(name: String, surname: String) {
    val id: Int = Math.random().toInt
    this(id, name, surname)
  }
}
