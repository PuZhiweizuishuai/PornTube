function i(n){if(!(n==null||n===""))return n>=8294400?"4K":n>=2073600?"HD":"SD"}function u(n){if(n==null)return"数据校验错误，请检查数据后重新提交";let r="";for(const t in n)r+=n[t]+`
`;return r}const e={clarityDisplay:i,dataErrorMessage:u};export{e as S,u as d};
