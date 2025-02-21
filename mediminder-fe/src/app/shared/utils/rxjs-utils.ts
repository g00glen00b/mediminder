import {OperatorFunction, reduce} from "rxjs";


export function reduceToArrayByProperties<S, M1, M2>(selector1: (source: S) => M1, selector2: (source: S) => M2): OperatorFunction<S, [M1[], M2[]]> {
  return reduce((result, source) => [
    [...result[0], selector1(source)],
    [...result[1], selector2(source)]
  ], [[], []] as [M1[], M2[]]);
}
