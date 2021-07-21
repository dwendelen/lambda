package se.daan.lambda

import se.daan.lambda.runner.Runner

fun main() {
    val thing = """
        Bool : A -> A -> A
        true : Bool = t -> f ->
            t
        false : Bool = t -> f ->
            f
        if : Bool -> A -> A -> A = cond -> t -> f ->
            cond.t.f
        not : Bool -> Bool = b ->
            b.false.true
        and : Bool -> Bool -> Bool = a -> b ->
            a.b.a
        or : Bool -> Bool -> Bool = a -> b ->
            a.a.b
        xor : Bool -> Bool -> Bool = a -> b ->
            a.(not.b).b
        boolEq : Bool -> Bool -> Bool = a -> b ->
            a.b.(not.b)
        printBool : Bool -> IO = a ->
            a.(printTrue).(printFalse)

        person : N -> A -> (N -> A -> R) -> R = name -> age ->
            fn ->
            fn.name.age
        name : N -> _ -> N = name -> age ->
            name
        age : _ -> A -> A = name -> age ->
            age
        
        List.I : (I -> List.I -> R) -> R -> R
        list : I -> List.I -> List.I = item -> tail -> 
            onItem -> onNil ->
                onItem.item.tail
        nil : List.I = onItem -> onNil ->
            onNil
            
        map1 : Self -> (I -> O) -> List.I -> List.O = self ->
            fn -> node -> 
              node.(
                i -> t -> 
                    list.(fn.i).(self.self.fn.t)
              ).(
                nil
              )
        map: (I -> O) -> List.I -> List.O = map1.map1

        fold1 : Self -> (A -> I -> A) -> A -> List.I -> A = self ->
            fn -> acc -> node ->
                node.(
                    item -> tail ->
                        self.self.fn.(fn.acc.item).tail
                ).(
                    acc
                )
        fold : (A -> I -> A) -> A -> List.I -> A = fold1.fold1
        
        foldr1 : Self -> (I -> A -> A) -> A -> List.I -> A = self ->
            fn -> acc -> node ->
                node.(
                    item -> tail ->
                        fn.item.(self.self.fn.acc.tail)
                ).(
                    acc
                )
        foldr : (I -> A -> A) -> A -> List.I -> A = foldr1.foldr1
        
        printList : (A -> IO) -> List.A -> IO -> IO = printElement -> node -> cnt ->
            printLBracket.(
                foldr.(e -> c -> printElement.e.(printComma.c)).(printRBracket.cnt).node
            )            
        
        all : List.Bool -> Bool = fold.and.true
        
        Bit: R -> R -> R
        0 : Bit = on0 -> on1 -> on0
        1 : Bit = on0 -> on1 -> on1
        plusBit1 : Bit -> Bit -> Bit -> Bit = a -> b -> c ->
            a.(
                b.(
                    c.(
                        fn -> fn.0.0
                    ).(
                        fn -> fn.0.1
                    )
                ).(
                    c.(
                        fn -> fn.0.1
                    ).(
                        fn -> fn.1.0
                    )
                )
            ).(
                b.(
                    c.(
                        fn -> fn.0.1
                    ).(
                        fn -> fn.1.0
                    )
                ).(
                    c.(
                        fn -> fn.1.0
                    ).(
                        fn -> fn.1.1
                    )
                )
            )
        plusBit : Bit -> Bit -> Bit = a -> b ->
            plusBit1.0.a.b.(c -> r -> r)
        printBit : Bit -> IO = a -> 
            a.(print0.a).(print1.a)
        
        Num : List.Bit
        num : Bit -> Num = b -> list.b.nil
        plusNum1 : Self -> Num -> Num -> Bit = self ->
            a -> b -> c ->
                a.(
                    ia -> ta -> 
                        b.(
                            ib -> tb ->
                                plusBit1.ia.ib.c.(
                                    newC -> d -> list.d.(self.self.ta.tb.newC)
                                )
                        ).(
                            plusBit1.ia.0.c.(
                                newC -> d -> list.d.(self.self.ta.nil.newC)
                            )
                        )
                ).(
                    b.(
                        ib -> tb ->
                            plusBit1.0.ib.c.(
                                newC -> d -> list.d.(self.self.nil.tb.newC)
                            )
                    ).(
                        nil
                    )
                )
        plusNum : Num -> Num -> Num = A -> B -> plusNum1.plusNum1.A.B.0
        printNum : Num -> IO = num -> printList.printBit.num
        
        num0 : Num = num.0
        num1 : Num = num.1
        num2 : Num = plusNum.num1.num1
        
        infiniteList1 : Self -> List.Bool = self -> list.true.(self.self)
        infiniteList : List.Bool = infiniteList1.infiniteList1
        
        take0 : List.I -> List.I = node -> nil
        take1 : List.I -> List.I = node -> node.(
                item -> tail -> list.item.(take0.tail)
            ).(
                nil
            )
        take2 : List.I -> List.I = node -> node.(
                item -> tail -> list.item.(take1.tail)
            ).(
                nil
            )
        take3 : List.I -> List.I = node -> node.(
                item -> tail -> list.item.(take2.tail)
            ).(
                nil
            )
        
        prgm8 : IO = printNum.num2
        prgm9 : IO = printBool.true.(printLn.(printBool.(xor.true.true).(printLn.done)))
        prgm : IO = printList.printBool.(take3.(map.not.infiniteList)).done
        prgm5 : IO = printList.printBool.(take1.(list.true.(list.false))).done
        prgm8 : IO = printBool.(not.true).done
        prgm7 : IO = (a -> a).done
    """.trimIndent() + "\n"

    Runner()
        .io("printLn") { println() }
        .io("printTrue") { print("true") }
        .io("printFalse") { print("false") }
        .io("print0") { print("0") }
        .io("print1") { print("1") }
        .io("printLBracket") { print("[") }
        .io("printRBracket") { print("]") }
        .io("printComma") { print(",") }
        .run(thing, "prgm")
}